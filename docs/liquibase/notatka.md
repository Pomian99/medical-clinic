# Liquibase (MED-14)

## L38.1: schemat z bazy

Świeża baza, tabele założył jeszcze Hibernate (`ddl-auto=update`). Generator zapisał schemat w
`src/main/resources/liquibase-changelog/versions/001-initial.yaml`.

### Kroki w pliku: 17, a nie 18

| Rodzaj kroku | Liczba | Obiekty w bazie |
|---|---|---|
| `createTable` | 6 | `doctors`, `users`, `patients`, `facilities`, `doctor_facility`, `visits` |
| `addUniqueConstraint` | 5 | `doctors.user_id`, `users.email`, `patients.user_id`, `patients.id_card_no`, `facilities.name` |
| `addForeignKeyConstraint` | 6 | dwa w `doctor_facility`, dwa w `visits`, po jednym w `doctors` i `patients` |

Każdy krok ma `author: kursant`. Generator przepisał wszystko, co jest w bazie (6 tabel, 5 ograniczeń
unikalności, 6 kluczy obcych), więc 17 to pełny schemat tego projektu. Zadanie liczy 18 kroków dla
schematu z kursu, który ma o jedno ograniczenie więcej. Dalsze numery przesuwają się o jeden:
kroki z L38.5 dostaną numery 18 i 19, licznik po starcie pokaże `Run: 19`, a cofnięcie obejmie 19 kroków.

### Ograniczenia check

| W bazie (założył Hibernate) | W pliku |
|---|---|
| `doctors_specialization_check`: `specialization IN ('CARDIOLOGIST', 'DERMATOLOGIST', 'PEDIATRICIAN')` | brak |
| `users_role_check`: `role IN ('PATIENT', 'DOCTOR')` | brak |

`grep -ci 'check'` na pliku daje 0: generator zgubił oba ograniczenia. Kolumny z enumem są w pliku
zwykłym `VARCHAR(255)`, więc baza założona z samej historii przyjmie dowolny tekst. Oba ograniczenia
wracają w L38.5 jako osobny krok `002-enum-checks.yaml`.

## Co się zmieniło (22.11.2026)

Historia ma dziś 23 kroki, więc start na świeżej bazie pokazuje `Run: 23`:

| Krok | Plik | Co robi |
|---|---|---|
| 18 | `002-enum-checks.yaml` | ograniczenia check roli i specjalizacji (L38.5) |
| 19 | `003-visits-patient-id-index.yaml` | indeks na `visits.patient_id` (L38.5) |
| 20, 21 | `004-admin.yaml` | rola `ADMIN` w checku i konto admina (MED-16, MED-17) |
| 22 | `005-visits-doctor-start-unique.yaml` | unikalność (lekarz, start) wizyty (poprawka po recenzji PR #31) |
| 23 | `006-readable-unique-constraint-names.yaml` | pięć ograniczeń unikalności z kroków 5–9 dostało czytelne nazwy, np. `uk_users_email` (poprawka po recenzji PR #31) |
