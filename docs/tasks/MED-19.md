# Lekcja 22. Relacje N:1 i 1:N. Ćwiczenia
## MED-19 część 1 i MED-20 część 1

### Zadanie 1 (10 min). Doktor

Doktor to druga rola, która korzysta z konta z lekcji 21.

1. W pakiecie `model` utwórz enum `Specialization` z wartościami `CARDIOLOGIST`,
   `DERMATOLOGIST`, `PEDIATRICIAN`.
2. Utwórz encję `Doctor` (tabela `doctors`): `id`, konto `user` z relacją 1:1 taką samą jak
   u pacjenta, `firstName`, `lastName` i `specialization` zapisywaną nazwą.
3. Utwórz `DoctorRepository`.
4. Dopisz na końcu `data.sql` konto z rolą `DOCTOR` (`katarzyna.wrobel@example.com`) i doktora
   Katarzynę Wróbel, kardiologa, na tym koncie.

Kryteria akceptacji:
- [ ] Kolumna `specialization` przechowuje nazwę (`CARDIOLOGIST`), a nie liczbę.
- [ ] `doctors.user_id` ma `UNIQUE` i klucz obcy do `users`, tak jak `patients.user_id`.
- [ ] Aplikacja startuje dwa razy z rzędu bez błędu `data.sql`.

### Zadanie 2 (10 min). Wizyta z dwiema relacjami

Wizyta ma jednego doktora i najwyżej jednego pacjenta. Wizyta bez pacjenta to wolny slot.

1. Utwórz encję `Visit` (tabela `visits`) z polami `startsAt` i `endsAt` (`LocalDateTime`,
   wymagane) oraz polami `doctor` i `patient`, każde jako relacja wiele do jednego.
2. Doktor ma być wymagany, pacjent nie.
3. Utwórz `VisitRepository`.

Kryteria akceptacji:
- [ ] W `\d visits` widzisz dwa klucze obce: `doctor_id` z `not null` i `patient_id` bez niego.
- [ ] Na żadnym z nich nie ma `UNIQUE`.
- [ ] W bazie są dokładnie cztery tabele: `users`, `patients`, `doctors`, `visits`.

### Zadanie 3 (10 min). `POST /visits` zakłada wolny slot

Dopisz warstwę web dla wizyt: `VisitCreateCommand` (`doctorId`, `startsAt`, `endsAt`, wszystkie
wymagane), `VisitDto` (`id`, `doctorId`, `patientId`, `startsAt`, `endsAt`), `VisitMapper`,
`DoctorNotFoundException` (404), `VisitService` i `VisitController`. Reguł slotów (przyszłość,
pełny kwadrans, nakładanie) jeszcze nie ma: to lekcja 25.

Kryteria akceptacji:
- [ ] `POST /visits` z doktorem 1 i godzinami → 201, w odpowiedzi `"patientId": null`.
- [ ] W tabeli `visits` jest wiersz z `doctor_id = 1` i pustym `patient_id`.
- [ ] `POST /visits` z `doctorId` 999 → 404 z komunikatem o doktorze.
- [ ] `POST /visits` bez `doctorId` → 400 z polem `doctorId` na liście błędów.

## Jak sprawdzisz, że skończyłeś

- `docker compose down -v`, `docker compose up -d --wait`, uruchomienie aplikacji → startuje
- `SELECT id, last_name, specialization FROM doctors;` → Wróbel, `CARDIOLOGIST`
- `POST /visits` → 201, `POST /visits` z nieznanym doktorem → 404, bez doktora → 400
- `mvn test` → `BUILD SUCCESS`

## Zadanie D1. Doktor z istniejącego konta

Konto doktora zakłada recepcja (`POST /users` z lekcji 21), a rolę doktora nadaje się potem.
To jest wariant B z lekcji 21. Dopisz `/doctors` przez wszystkie warstwy: `POST` przyjmuje
`userId`, imię, nazwisko i specjalizację.

Kryteria akceptacji:
- [ ] `POST /doctors` z `userId` istniejącego konta → 201, w odpowiedzi `id`, `userId`,
  `email` (z konta), imię, nazwisko i specjalizacja, bez hasła.
- [ ] `POST /doctors` z `userId`, którego nie ma → 404.
- [ ] `POST /doctors` z `userId` konta, które ma już doktora → 409.
- [ ] `GET /doctors` → 200, `GET /doctors/{id}` → 200, nieistniejący → 404.
- [ ] `DELETE /doctors/{id}` doktora bez wizyt → 204, doktora z wizytami → 409 (wizyta bez
  doktora nie istnieje).
- [ ] `DELETE /users/{id}` konta doktora → 409, tak jak konta pacjenta.

## Zadanie D2. Wizyty doktora

Dopisz `GET /visits?doctorId=...`, który zwraca wizyty jednego doktora. Zapytanie napisz nazwą
metody w repozytorium wizyt. Potem dopisz w `Doctor` listę wizyt jako stronę „jeden" relacji.

Kryteria akceptacji:
- [ ] `GET /visits?doctorId=1` → 200 i wizyty doktora 1.
- [ ] W logu zapytanie ma warunek `where v1_0.doctor_id=?`, bez złączenia z `doctors`.
- [ ] Po dopisaniu listy wizyt w `Doctor` i `docker compose down -v` w bazie nadal są tylko
  cztery tabele (żadnej `doctors_visits`).

## Zadanie D3. Foldery `doktorzy` i `wizyty` w kolekcji Bruno

Scenariusz z kompendium: konto, doktor na tym koncie, doktor na zajęte konto (błąd).

Kryteria akceptacji:
- [ ] Folder `doktorzy`: utworzenie konta z rolą `DOCTOR`, doktor na tym koncie (201), drugi
  doktor na to samo konto (409), doktor na nieistniejące konto (404), lista doktorów.
- [ ] Folder `wizyty`: wolny slot (201), slot u nieistniejącego doktora (404), wizyty doktora
  (200), kasowanie doktora z wizytami (409).
- [ ] Każde żądanie ma asercję statusu (zakładka **Assert**: `res.status`, `eq`, oczekiwany
  kod). Bez niej Bruno wypisuje `PASS` także wtedy, gdy zamiast 201 przyszło 409.
- [ ] Żądania tworzące doktora nie wpisują numeru konta na sztywno. Każdy start aplikacji
  przesuwa numery kont, bo `INSERT ... ON CONFLICT DO NOTHING` w `data.sql` zużywa
  wartości sekwencji także wtedy, gdy wiersz już jest. W żądaniu „Utwórz konto doktora"
  otwórz zakładkę **Script**, pole **Post Response**, i wpisz
  `bru.setVar("kontoDoktoraId", res.body.id);`. W body dwóch kolejnych żądań wstaw
  `"userId": {{kontoDoktoraId}}`.
- [ ] Po `docker compose down -v` oba foldery przechodzą przy pierwszym przebiegu, także wtedy, gdy po skasowaniu wolumenu uruchomisz aplikację kilka razy. Drugi przebieg folderu `doktorzy` na tej samej bazie ma paść na asercjach, bo konto i doktor już istnieją.

## Jak sprawdzisz, że skończyłeś

Skasuj wolumen, podnieś bazę, uruchom aplikację i puść foldery z terminala:

```bash
cd bruno
npx @usebruno/cli@4.1.0 run doktorzy --env local
npx @usebruno/cli@4.1.0 run wizyty --env local
```

Wszystkie asercje mają przejść (`Requests: 5 (5 Passed)` i `4 (4 Passed)`). `mvn test` przechodzi przy zatrzymanym kontenerze.

## Lektury

- `baeldung.com/hibernate-one-to-many`: relacja jeden do wielu krok po kroku, całość.
