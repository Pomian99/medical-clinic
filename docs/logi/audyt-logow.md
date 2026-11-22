# Audyt pliku logu (MED-30 cz. 2, L37.6)

Czysta baza, `rm -rf logs`, aplikacja bez profilu (próg INFO). Audyt dotyczy `logs/medical-clinic.log`,
a nie konsoli: SQL z `show-sql` idzie printlnem tylko na konsolę (w pliku 0 linii `Hibernate:`,
na konsoli 34).

## Żądania

| Żądanie | Status | Wpis w pliku |
|---|---|---|
| `POST /patients` (hasło `AuditPass1!`, dowód `QWE987654`) | 201 | `[9ff9911f] … Registered patient 5` |
| `PATCH /patients/5/password` (hasło `ChangedPass2@`) | 200 | `[32cd742c] … Changed password of patient 5` |
| `POST /facilities` | 201 | `[5e17d59a] … Created facility 3` |
| `POST /users` (hasło `AccountPass3#`) | 201 | `[c6b64def] … Created user 6` |
| `POST /patients` drugi raz, ten sam e-mail | 409 | `[1266f473] WARN … Domain error: User with email audyt.pacjent@example.com already exists` |

## Wyniki

| Szukane | Trafienia |
|---|---|
| `password=`, `idCardNo`, `id_card_no` | 0 |
| wysłane hasła i numer dowodu (`AuditPass1!`, `ChangedPass2@`, `AccountPass3#`, `QWE987654`) | 0 |
| telefon, imię, nazwisko pacjenta | 0 (poza adresem e-mail poniżej) |
| adresy e-mail | **1**: WARN z advice przy 409 |

Haseł, numeru dowodu ani całych obiektów w pliku nie ma: serwisy logują same identyfikatory.

## E-mail w logu: co z tym zrobić

E-mail trafia do logu nie przez serwis, tylko przez komunikat wyjątku domenowego, który advice zapisuje
jako WARN. Dziś adres wkładają do komunikatu trzy wyjątki:

- `UserAlreadyExistsException` („User with email … already exists”, 409 przy rejestracji i `POST /users`),
- `PatientNotFoundException(String email)` („Patient with email … not found”, 404 przy `GET /patients?email=`),
- `UserNotFoundException` w wariancie z e-mailem.

E-mail to dana osobowa. Log trafia na serwer i do narzędzi zbierających logi, więc nie powinno go tam być.

Proponowana poprawka: usunąć adres z komunikatów tych wyjątków, np. „User with this email already
exists”. Klient i tak wie, jaki e-mail wysłał, więc odpowiedź niczego nie traci, a WARN w logu dalej
mówi, co się stało. Identyfikator żądania w nawiasie pozwala powiązać wpis z konkretnym żądaniem.
Trzeba przy tym poprawić testy, które sprawdzają pełny komunikat (`PatientServiceTest`,
`PatientControllerTest`).

**Wprowadzone.** Wszystkie trzy wyjątki mają teraz konstruktory bez e-maila i komunikaty
„User with this email already exists”, „Patient with this email not found”, „User with this email not
found”. Po poprawce 409 przy rejestracji oraz 404 przy `GET /patients?email=` i `GET /users?email=`
dają w pliku logu trzy wpisy WARN i zero adresów e-mail.

Odrzucone alternatywy:

- logowanie w advice samego typu wyjątku: usuwa e-mail, ale też identyfikatory z wszystkich innych
  błędów domenowych, które są w logu przydatne;
- maskowanie adresu w advice (np. `a***@example.com`): działa, ale każdy nowy wyjątek z daną osobową
  wymagałby pamiętania o masce.

## Co się zmieniło (22.11.2026, poprawki po recenzji PR #31)

Recenzent znalazł dwie drogi, którymi dane z bazy trafiały do logu. Ten audyt ich nie złapał, bo
nie było w nim rejestracji z zajętym numerem dowodu:

- zajęty numer dowodu kończył się 500 i stack trace'em z `Key (id_card_no)=(...) already exists`;
- Hibernate logował na WARN pełny komunikat błędu PostgreSQL, razem z linią `Detail:` z wartościami.

Poprawki:

- `show-sql` usunięty (3eadf1f): SQL idzie przez logger `org.hibernate.SQL` i tylko w profilu `dev`,
  więc trafia też do pliku i ma `[requestId]`. Zdanie z początku notatki o `println` na konsolę dotyczy
  stanu sprzed tej zmiany.
- `logServerErrorDetail=false` dla sterownika PostgreSQL, a naruszenia `UNIQUE` i `FOREIGN_KEY` dają
  409 i wpis WARN z samą nazwą ograniczenia, bez stack trace'a (78373ae).
- Zajęty numer dowodu odrzuca serwis przed zapisem: 409 z komunikatem bez numeru (1e83bcd).
- `toString` Commandów pokazuje `***` zamiast hasła i numeru dowodu (b65bd6c).

Sprawdzone 22.11 przy wyścigu 15 równoległych `POST /visits` na ten sam termin: przy każdym 409 jest
tylko WARN, w pliku zero linii `Detail:` i zero wpisów ERROR.
