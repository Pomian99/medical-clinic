# Lekcja 26. Transakcje, ACID i @Transactional. Ćwiczenia

## Na zajęciach (30 min): MED-26 część 1

### Zadanie 1 (10 min). Transakcje w dwóch serwisach

1. W `PatientService` i `VisitService` oznacz każdą metodę, która zmienia dane,
   adnotacją `@Transactional`, a każdą, która tylko czyta, `@Transactional(readOnly = true)`.
2. Adnotację importuj z pakietu `org.springframework.transaction.annotation`.

Kryteria akceptacji:
- [ ] W obu serwisach każda metoda publiczna ma jedną z dwóch adnotacji.
- [ ] `create`, `update`, `changePassword`, `deleteById` i `book` mają `@Transactional`,
  metody `find...` i `searchByName` mają `readOnly = true`.
- [ ] `grep -rn "import .*Transactional;" src/main/java` pokazuje wyłącznie
  `org.springframework.transaction.annotation.Transactional`.

### Zadanie 2 (12 min). Zapis bez save

1. Usuń `save` z `PatientService.update`, `PatientService.changePassword`
   i `VisitService.book`. Metody zwracają zmienioną encję.
2. Zostaw `save` w `PatientService.create` i `VisitService.create`.
3. Zbuduj, uruchom i sprawdź każdą zmianę w bazie, a nie tylko w odpowiedzi.

Kryteria akceptacji:
- [ ] `PUT /patients/1` z nowym nazwiskiem daje 200, a
  `SELECT id, last_name FROM patients WHERE id = 1;` pokazuje nowe nazwisko.
- [ ] `PATCH /patients/1/password` daje 200, a `SELECT id, password FROM users WHERE id = 1;`
  pokazuje nowe hasło.
- [ ] Rezerwacja wolnego slotu daje 200, a `SELECT id, doctor_id, patient_id FROM visits;`
  pokazuje pacjenta przy wizycie.
- [ ] Umiesz powiedzieć, czemu `create` nadal potrzebuje `save`.

### Zadanie 3 (8 min). Transakcja w logu

1. Dopisz do `src/main/resources/application.properties` wiersz
   `logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG`.
2. Zarejestruj nowego pacjenta (`POST /patients`, dane spoza `data.sql`).
3. Znajdź w `app.log` wiersz, który otwiera transakcję rejestracji, i wiersz, który ją
   zatwierdza.

Kryteria akceptacji:
- [ ] `grep 'Creating new transaction' app.log` pokazuje transakcję o nazwie
  `...PatientService.create`.
- [ ] Umiesz wskazać w logu, że zapytanie o zajęty e-mail i zapis pacjenta leżą między
  `Creating new transaction ... PatientService.create` a `Initiating transaction commit`.
  `Participating in existing transaction` stoi przed `save`; zapytanie z nazwy metody nie
  zostawia własnego wiersza.

## Jak sprawdzisz, że skończyłeś

Na czystej bazie (`docker compose down -v`, `docker compose up -d --wait`, start aplikacji):

- `PUT /patients/1` → 200, nowe nazwisko w `psql`
- `PATCH /patients/1/password` → 200, nowe hasło w `psql`
- nowy slot → 201, `PATCH /visits/1/book/2` → 200, `patient_id` równe 2 w `psql`
- `./mvnw test` → `BUILD SUCCESS`

## Podpowiedzi (czytaj dopiero, gdy utkniesz)

- Zadanie 1: IntelliJ podpowie też `jakarta.transaction.Transactional`. Ta adnotacja nie ma
  `readOnly`, więc przy `@Transactional(readOnly = true)` kompilator zgłosi błąd.
- Zadanie 2: jeśli odpowiedź ma nową wartość, a baza starą, metoda nie jest objęta
  transakcją. Sprawdź adnotację i jej import.
- Zadanie 2: nowa encja (zbudowana maperem albo builderem) nie jest zarządzana. Hibernate nie
  wie o niej, dopóki `save` jej nie przedstawi.
- Zadanie 3:
  `grep -oE 'Creating new transaction[^:]*|Participating.*|Initiating.*|Hibernate: .{0,60}' app.log | tail -6`.

## Zadanie D1. Zastępstwo doktora w jednej transakcji

Dopisz `POST /doctors/{id}/substitutions` z treścią `{"substituteId": 2}`. Wszystkie wizyty
doktora `{id}` przechodzą do zastępcy, od najwcześniejszej. Zastępca nie może mieć w tym czasie
własnej wizyty: konflikt z taką wizytą daje 409 i przerywa całą operację.

Kryteria akceptacji:
- [ ] Brak doktora albo zastępcy → 404, brak `substituteId` w treści → 400.
- [ ] Zastępstwo bez konfliktu → 200 z listą przeniesionych wizyt jako `VisitDto`.
- [ ] Konflikt przy drugiej wizycie → 409, a pierwsza wizyta zostaje przy starym doktorze.
- [ ] Metoda serwisu ma `@Transactional` i nie woła `save`.

## Zadanie D2. Udokumentowany rollback

Załóż plik `docs/transakcje/rollback.md` i opisz w nim dwa przebiegi zastępstwa z konfliktem
przy drugiej wizycie:

1. Wariant A: na chwilę zdejmij `@Transactional` z metody zastępstwa i dopisz w pętli
   `visitRepository.save(visit)`, tak jak pisało się serwisy przed tą lekcją.
2. Wariant B: stan docelowy z D1.

Kryteria akceptacji:
- [ ] Przy każdym wariancie jest wynik zapytania
  `SELECT id, doctor_id, starts_at FROM visits ORDER BY id;` po zastępstwie.
- [ ] Przy wariancie B są cztery wiersze logu: otwarcie transakcji zastępstwa, `update visits`,
  `Initiating transaction rollback` i `Rolling back JPA transaction`.
- [ ] Notatka wyjaśnia własnymi słowami, czemu w wariancie A zostaje stan w połowie, a w B nie,
  i czemu `update` w logu nie oznacza zmiany w bazie.
- [ ] Po pomiarze kod wraca do wariantu B.

## Zadanie D3. Transakcje w pozostałych serwisach

Oznacz metody `DoctorService`, `UserService` i `FacilityService` według konwencji z zajęć.
Usuń `save` tam, gdzie encja jest zarządzana (przypisanie i odpięcie placówki).

Kryteria akceptacji:
- [ ] Każda metoda publiczna w tych trzech serwisach ma `@Transactional` albo `@Transactional(readOnly = true)`.
- [ ] `POST /doctors/1/facilities/2` daje 200, a `SELECT * FROM doctor_facility;` pokazuje wiersz
  `1 | 2`.

## Zadanie D4. Asercje w starszych folderach Bruno

Foldery `pacjenci`, `konta` i `users` nie mają asercji, więc `PASS` Bruno nic nie mówi
o statusach. Dopisz blok `assert` z oczekiwanym statusem do każdego żądania. Dołóż folder
`zastepstwa` ze scenariuszem D1.

Kryteria akceptacji:
- [ ] Każde żądanie w trzech folderach ma `assert { res.status: eq ... }`.
- [ ] Folder `pacjenci` tworzy własnego pacjenta spoza `data.sql`, zapamiętuje jego numer
  (`bru.setVar("patientId", res.body.id);`) i dalej używa `{{patientId}}` zamiast `1` (poza żądaniami, które kończą się 400 na
  walidacji, zanim serwis szuka pacjenta).
- [ ] Folder `zastepstwa` zakłada konto i doktora zastępcę, trzy sloty (dwa u doktora 1
  o 10:00 i 11:00, jeden u zastępcy o 11:00), wysyła zastępstwo (409) i sprawdza w bloku
  `tests`, że obie wizyty doktora 1 zostały przy nim.
- [ ] Na świeżej bazie `pacjenci`, `konta` i `zastepstwa` przechodzą w całości, a `users`
  przechodzi ze środowiskiem `test`.

## Jak sprawdzisz, że skończyłeś

Skasuj wolumen, podnieś bazę, uruchom aplikację i puść foldery:

```bash
cd bruno
npx @usebruno/cli@4.1.0 run zastepstwa --env local
npx @usebruno/cli@4.1.0 run pacjenci --env local
npx @usebruno/cli@4.1.0 run konta --env local
npx @usebruno/cli@4.1.0 run users --env test
```

Każdy przebieg kończy się bez nieudanych żądań, a `zastepstwa` pokazuje `Tests: 1/1`.
Po ponownym zdjęciu `@Transactional` (i dopisaniu `save`) test w `zastepstwa` ma paść.
`./mvnw test` przechodzi przy zatrzymanym kontenerze.

## Lektury

- Wikipedia, hasło „ACID": cztery gwarancje w skrócie.
- `baeldung.com/transaction-configuration-with-jpa-and-spring`: jak Spring zarządza
  transakcjami; przeczytaj do sekcji o konfiguracji programowej (tej sekcji już nie).
