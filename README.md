# Medical Clinic

REST API for managing a medical clinic: patients, user accounts, doctors, facilities and
visits.

## Running Locally

You need Java 21 and Docker. The application stores its data in PostgreSQL. `compose.yaml`
starts a database with the name, user and password that
`src/main/resources/application.properties` expects:

1. `docker compose up -d --wait` - starts the database. Data is kept in the `clinic-data`
   volume between restarts; `docker compose down -v` removes it.
2. `./mvnw spring-boot:run` - starts the application at `http://localhost:8080`.

On start Liquibase builds the schema from the change log
`src/main/resources/liquibase-changelog/liquibase-changelog.yaml`. It runs only the change sets
the database has not seen yet and records them in the `databasechangelog` table. Hibernate does
not create or change tables: with `spring.jpa.hibernate.ddl-auto=validate` it only checks that
the entity classes match the schema, and the application does not start when they do not. A
schema change therefore needs a new file in `liquibase-changelog/versions/`, added to the change
log, not just a changed entity.

After the schema, `src/main/resources/data.sql` inserts sample accounts, patients, a doctor and
two facilities. The script runs on every start and skips rows that already exist.

Tests do not need Docker: `./mvnw test` runs them against an in-memory H2 database
configured in `src/test/resources/application.properties`. Liquibase builds the H2 schema from
the same change log.

## Logging In

Every request except registration and login needs a token. Get one from `POST /auth/token`
with the e-mail and password of an account, then send it in the `Authorization` header:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin","password":"admin"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/users
```

The login answers `{"token": "...", "tokenType": "Bearer", "expiresIn": 3600}`, so a token is
valid for an hour. A wrong password or an unknown e-mail gives 401. The signing keys live in
memory, so a restart of the application invalidates every token issued before it.

Demo accounts:

| E-mail | Password | Role | Comes from |
|---|---|---|---|
| `admin` | `admin` | `ADMIN` | change set 21 in `liquibase-changelog/versions/004-admin.yaml` |
| `anna.nowak@example.com` | `Silne123!` | `PATIENT` | `data.sql` |
| `jan.wisniewski@example.com` | `Silne456!` | `PATIENT` | `data.sql` |
| `ewa.kowalczyk@example.com` | `Silne789!` | `PATIENT` | `data.sql` |
| `katarzyna.wrobel@example.com` | `Silne321!` | `DOCTOR` | `data.sql`: doctor 1, also a patient on the same account |

The roles are checked in `config/SecurityConfig.java`. In short:

| Who | Endpoints |
|---|---|
| nobody logged in | `POST /patients` (registration works only without a token), `POST /auth/token` |
| `ADMIN` | `/users/**`, `GET /patients` (list and search), `GET` and `POST /doctors`, `POST /facilities`, `POST /visits` (new free slots), the API documentation, `GET /postman/**` |
| `DOCTOR` | `POST` and `DELETE /doctors/{id}/facilities/{facilityId}` |
| `PATIENT` | the remaining requests under `/patients/**`, `/doctors/**`, `/facilities/**` and `/visits/**` |
| any account | `GET /facilities/**` |

A request without a token gets 401, and a request the role does not allow gets 403.

## API Documentation

The API description is generated from the code by springdoc and is available at two addresses
while the application is running locally:

- `http://localhost:8080/v3/api-docs` - the OpenAPI description as JSON, for machines
  (Bruno, Postman and client generators can import it),
- `http://localhost:8080/swagger-ui/index.html` - a clickable page for humans.

Both are for the admin only and need the admin token (see [Logging In](#logging-in)), the
page and its scripts included. A browser does not send the token on its own, so opening the page
directly ends with 401 and no login window. Read the JSON with the token instead and save it to
a file for import:

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/v3/api-docs > api-docs.json
```

The documentation is disabled in production. The `prod` profile
(`src/main/resources/application-prod.properties`) sets
`springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`, so both
addresses return 404 even with the admin token (401 without a token, like every other address),
while the API itself keeps working. Activate the profile with
`java -jar target/medical-clinic-<version>.jar --spring.profiles.active=prod`, where
`<version>` is the project version from `pom.xml`.

**What it does not check:** the OpenAPI description says what the API *declares*, not what
it *does*. Nothing executes it, so an endpoint can declare a `409` and never return it, and
the description will not notice. The Bruno collection in `bruno/` verifies what the API
actually does and should be run after every change. One does not replace the other.

## Bruno Collection

The application folders need the running application, a fresh database and the `local`
environment, which holds the demo accounts. `login` goes first, because it saves the tokens the
other folders send. Run them from the `bruno` directory:

```bash
npx @usebruno/cli@4.1.0 run login patients users doctors visits facilities slots substitutions visit-pages postman --env local
```

`jsonplaceholder-users` is an exercise on JSONPlaceholder, a public test API, and runs with the
`test` environment:

```bash
npx @usebruno/cli@4.1.0 run jsonplaceholder-users --env test
```

The application serves a Postman export of the collection to the admin at `GET /postman/patients`.
The file is `src/main/resources/medical-clinic.postman_collection.json`, exported from Bruno
(Share → Postman), so it has to be exported again after the collection changes.
