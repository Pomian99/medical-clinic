## MED-30 cz. 2: logi w całej aplikacji, MDC i audyt

Ciąg dalszy MED-30 (L37), te same zasady wpisów co w cz. 1.

**L37.4. Pozostałe serwisy i fasada**
- INFO w `DoctorService` (utworzenie, usunięcie, przypisanie i odpięcie placówki),
  `UserService` (utworzenie i usunięcie konta), `FacilityService` (utworzenie placówki).
- `VisitBookingFacade`: jeden DEBUG o złożeniu podsumowania (wizyta, doktor). Rezerwacji
  i wolnych slotów nie powtarzasz, zapisuje je `VisitService`.
- `./mvnw test` zielony.

**L37.5. `RequestIdFilter`** (pakiet `config`, `@Component`, dziedziczy po `OncePerRequestFilter`)
- Krótki identyfikator żądania (np. 8 znaków UUID) w MDC pod kluczem `requestId` i w nagłówku
  odpowiedzi `X-Request-Id`; `MDC.remove("requestId")` w `finally`.
- `logging.pattern.correlation=[%X{requestId}] ` (spacja na końcu, ginie przy kopiowaniu z PDF-u).
- `logging.file.name=logs/medical-clinic.log`, katalog `logs/` w `.gitignore`.
- Każda linia logu z żądania ma identyfikator w nawiasie; puste `[]` tylko przy wpisach ze startu.

**L37.6. Audyt pliku logu**
- Zarejestruj pacjenta, zmień mu hasło (`PATCH /patients/{id}/password`), załóż placówkę i konto (`POST /users`).
- `grep -riE 'password=|idCardNo|id_card_no' logs/` nic nie zwraca; nie ma też wysłanych haseł
  ani numeru dowodu (szukaj ich wartości).
- Sprawdź, czy w pliku jest e-mail (np. z WARN przy 409), i zapisz w komentarzu do pracy, co z tym zrobić.
- Audyt dotyczy `logs/`, nie `app.log` (tam SQL z `show-sql` pisany printlnem na konsolę).

Gotowe, gdy (czysta baza, `rm -rf logs`, build, aplikacja):
- `POST /patients` → 201, w odpowiedzi nagłówek `X-Request-Id`
- `grep -E 'utworzono|zarejestrowano' logs/medical-clinic.log` → wpis o rejestracji z tym samym identyfikatorem
- `grep -riE 'password=|idCardNo|id_card_no|<twoje-haslo>|<twoj-nr-dowodu>' logs/ | wc -l` → 0
- `git status` nie pokazuje `logs/`
- `./mvnw test` → `BUILD SUCCESS`
