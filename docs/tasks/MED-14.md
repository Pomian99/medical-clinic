## MED-14: Liquibase

Przed startem: aplikacja zatrzymana, baza od zera.

**L38.1. Schemat z bazy**
- Build i start na świeżej bazie (tabele zakłada jeszcze Hibernate); gdy `grep 'Started' app.log`
  coś pokaże, zatrzymaj aplikację.
- Sprawdź, jakie ograniczenia check (listy dozwolonych wartości) Hibernate założył na kolumnach z enumem:
  `docker exec clinic-db psql -U clinic -d clinic -c "SELECT conrelid::regclass AS tabela, conname FROM pg_constraint WHERE contype = 'c' AND conrelid <> 0 ORDER BY conname"`.
- Generator Liquibase'a zapisuje schemat jako pierwszy krok historii (polecenie niżej).
- `grep -c 'changeSet:' $PLIK` → 18, każdy krok ma `author: kursant`.
- Porównaj ograniczenia check w bazie i w pliku (`grep -ci 'check' $PLIK`) i zapisz, których brakuje: wrócą w L38.5.

```bash
PLIK=src/main/resources/liquibase-changelog/versions/001-initial.yaml
./mvnw -q org.liquibase:liquibase-maven-plugin:5.0.3:generateChangeLog \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/clinic \
  -Dliquibase.username=clinic -Dliquibase.password=clinic \
  -Dliquibase.changeSetAuthor=kursant \
  -Dliquibase.outputChangeLogFile=$PLIK
```

**L38.2. Liquibase rządzi, Hibernate pilnuje**
- `spring-boot-starter-liquibase` w `pom.xml` pod sterownikiem PostgreSQL, bez wersji.
- Spis `liquibase-changelog/liquibase-changelog.yaml` z jawnym `include` pliku `versions/001-initial.yaml`
  (`relativeToChangelogFile: true`).
- W `application.properties`: `spring.liquibase.change-log=classpath:liquibase-changelog/liquibase-changelog.yaml`,
  `spring.jpa.hibernate.ddl-auto=validate`; linię `spring.jpa.defer-datasource-initialization=true`
  usuń razem z komentarzem nad nią. `spring.sql.init.mode=always` zostaje: `data.sql` wykona się po Liquibasie.
- Trzy starty: na bazie z L38.1 pada z `relation "doctors" already exists`; na świeżej bazie
  `Run: 18` i 4 pacjentów; jeszcze raz bez czyszczenia `Run: 0`, `Previously run: 18`.
- Licznik kroków w logu: `grep -E 'liquibase.util +: (Run|Previously run|Total change sets):' app.log | sed 's/.* : //'`.
- Na świeżej bazie `Schema validation: missing table [...]`? W pliku została flaga `defer-datasource-initialization`.

**L38.3. Testy jadą historią**
- `./mvnw test` → `Errors: 2` i `no changelog could be found at 'classpath:/db/changelog/db.changelog-master.yaml'`.
- Na końcu `src/test/resources/application.properties` ten sam wpis spisu i `spring.jpa.hibernate.ddl-auto=validate`
  (plik testów zastępuje główny, a nie uzupełnia go) → `Tests run: 51`, `BUILD SUCCESS`.

**L38.4. Przepisy na cofnięcie**
- Każdy krok w `001-initial.yaml` dostaje sekcję `rollback`: `dropTable` do `createTable`,
  `dropUniqueConstraint` do `addUniqueConstraint`, `dropForeignKeyConstraint` do `addForeignKeyConstraint`.
  Nazwy tabel i ograniczeń przepisz z kroku, który cofasz.
- Identyfikatorów z generatora jeszcze nie ruszasz: porządkujesz je w MED-18.

**L38.5. Code review historii**
- Każda poprawka to nowy krok w osobnym pliku w `versions/`, z wpisem na końcu spisu i z sekcją `rollback`.
- `002-enum-checks.yaml`, krok `19`: ograniczenia check, które zgubił generator. Zmiana `sql`
  (`ALTER TABLE ... ADD CONSTRAINT ... CHECK (... IN (...))`, wartości z enumów w kodzie); jej
  Liquibase nie cofnie sam, więc przepis piszesz wprost.
- `003-...-index.yaml`, krok `20` (nazwę dokończ sam): indeks pod kolumną, po której filtruje jedno
  z zapytań `VisitRepository`, a indeksu nie ma (`\di` w `psql` pokaże istniejące).
- Komentarz w spisie: której reguły wizyt baza nie pilnuje i kto ją dziś sprawdza. Bez udawania gwarancji.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `./mvnw test` → `Tests run: 51`, `BUILD SUCCESS`
- `grep -c 'rollback:' src/main/resources/liquibase-changelog/versions/001-initial.yaml` → 18
- licznik kroków w logu → `Run: 20`, a `SELECT count(*) FROM patients` → 4
- `SELECT id, filename FROM databasechangelog WHERE orderexecuted IN (19, 20) ORDER BY orderexecuted` → `19` i `20` w nowych plikach
- zapytanie o ograniczenia check z L38.1 → wśród nich `doctors_specialization_check` i `users_role_check`
- na 20 000 wizyt plan zapytania o wizyty pacjenta używa twojego indeksu (polecenia niżej, aplikacja zatrzymana)
- cofnięcie 20 kroków zostawia tylko `databasechangelog` i `databasechangeloglock`, a następny start znowu daje `Run: 20` i 4 pacjentów

```bash
docker exec clinic-db psql -U clinic -d clinic -c "INSERT INTO visits \
  (starts_at, ends_at, version, doctor_id, patient_id) SELECT timestamp '2031-01-01 06:00' \
  + n * interval '1 hour', timestamp '2031-01-01 06:30' + n * interval '1 hour', 0, 1, \
  1 + n % 4 FROM generate_series(0, 19999) n" -c 'ANALYZE visits'
docker exec clinic-db psql -U clinic -d clinic \
  -c 'EXPLAIN (COSTS OFF) SELECT * FROM visits WHERE patient_id = 2'

./mvnw -q org.liquibase:liquibase-maven-plugin:5.0.3:rollback \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/clinic \
  -Dliquibase.username=clinic -Dliquibase.password=clinic \
  -Dliquibase.searchPath=src/main/resources \
  -Dliquibase.changeLogFile=liquibase-changelog/liquibase-changelog.yaml \
  -Dliquibase.rollbackCount=20
docker exec clinic-db psql -U clinic -d clinic -c '\dt'
```

Cofnięcie staje na `No inverse to liquibase.change.core.RawSQLChange created`? Krok ze zmianą `sql`
nie ma sekcji `rollback`.
