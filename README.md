# Medical Clinic

REST API for managing a medical clinic: patients, user accounts, doctors, facilities and
visits.

## Running Locally

The application stores its data in PostgreSQL. `compose.yaml` starts a database with the
name, user and password that `src/main/resources/application.properties` expects:

1. `docker compose up -d --wait` - starts the database. Data is kept in the `clinic-data`
   volume between restarts; `docker compose down -v` removes it.
2. `./mvnw spring-boot:run` - starts the application at `http://localhost:8080`.

On start Hibernate creates the missing tables from the entity classes, and
`src/main/resources/data.sql` inserts sample users, patients, a doctor and two facilities.
The script runs on every start and skips rows that already exist.

Tests do not need Docker: `./mvnw test` runs them against an in-memory H2 database
configured in `src/test/resources/application.properties`.

## API Documentation

The API description is generated from the code by springdoc and is available at two addresses
while the application is running locally:

- `http://localhost:8080/v3/api-docs` - the OpenAPI description as JSON, for machines
  (Bruno, Postman and client generators can import it),
- `http://localhost:8080/swagger-ui/index.html` - a clickable page for humans.

The documentation is disabled in production. The `prod` profile
(`src/main/resources/application-prod.properties`) sets
`springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`, so both
addresses return 404 while the API itself keeps working. Activate the profile with
`java -jar target/medical-clinic-<version>.jar --spring.profiles.active=prod`, where
`<version>` is the project version from `pom.xml`.

**What it does not check:** the OpenAPI description says what the API *declares*, not what
it *does*. Nothing executes it, so an endpoint can declare a `409` and never return it, and
the description will not notice. The Bruno collection in `bruno/` verifies what the API
actually does and should be run after every change. One does not replace the other.
