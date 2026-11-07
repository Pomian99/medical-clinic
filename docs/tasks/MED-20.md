# Lekcja 23. Relacje M:N, synchronizacja, LAZY i EAGER. Ćwiczenia

## MED-20 część 2

### Zadanie 1 (10 min). Placówka i relacja wiele do wielu

Doktor przyjmuje w kilku placówkach, placówka gości wielu doktorów.

1. Utwórz encję `Facility` (tabela `facilities`): `id`, `name` (wymagana i unikalna), `city`
   (wymagane), `zipCode`, `street`, `number`. Do tego `FacilityRepository`.
2. W `Doctor` dodaj zbiór placówek z relacją wiele do wielu przez tabelę `doctor_facility`
   (kolumny `doctor_id` i `facility_id`). Doktor jest właścicielem.
3. W `Facility` dodaj stronę odwrotną: zbiór doktorów z `mappedBy`.
4. Dopisz na końcu `data.sql` dwie placówki: „Przychodnia Centrum" i „Przychodnia Nowa Huta".

Kryteria akceptacji:
- [ ] `\d doctor_facility` pokazuje dwie kolumny kluczy obcych i `PRIMARY KEY (doctor_id,
      facility_id)`.
- [ ] W encjach są zbiory (`Set`), a nie listy. Jeśli pole było wcześniej listą, najpierw
  `docker compose down -v`.
- [ ] Aplikacja startuje dwa razy z rzędu bez błędu `data.sql`.

### Zadanie 2 (12 min). Metody synchronizujące i endpointy

1. W `Doctor` napisz `addFacility(Facility)` i `removeFacility(Facility)`. Każda zmienia obie
   strony relacji.
2. W `DoctorService` dopisz przypisanie i odpięcie placówki (404, gdy nie ma doktora albo
   placówki; zapis jawny).
3. W `DoctorController` dopisz `POST /doctors/{id}/facilities/{facilityId}` i `DELETE` na tej
   samej ścieżce. Oba zwracają doktora.
4. Dodaj do `DoctorDto` posortowaną listę nazw placówek.

Kryteria akceptacji:
- [ ] `POST /doctors/1/facilities/1` → 200, w odpowiedzi `"facilities": ["Przychodnia Centrum"]`.
- [ ] To samo żądanie drugi raz → 200, a w `doctor_facility` nadal jeden wiersz.
- [ ] Po `POST /doctors/1/facilities/2` doktor ma dwie placówki; `DELETE /doctors/1/facilities/2`
  → 200, znika jeden wiersz, a w logu jest
  `delete from doctor_facility where doctor_id=? and facility_id=?`.
- [ ] `POST /doctors/1/facilities/999` → 404.
- [ ] Nigdzie poza `Doctor` nie ma `getFacilities().add(...)`.

### Zadanie 3 (8 min). Policz zapytania

Uruchom aplikację z terminala z logiem do pliku (`./mvnw -q -DskipTests package`, potem
`java -jar target/medical-clinic-0.0.1-SNAPSHOT.jar > app.log 2>&1`). Załóż dwóch kolejnych doktorów (konto przez `POST /users`, doktor przez `POST /doctors`), przypisz
wszystkim trzem placówki, zrestartuj aplikację i wyślij jedno `GET /doctors`. Policz w logu wiersze
`Hibernate: select`.

```bash
grep -c 'Hibernate: select' app.log
```

Kryteria akceptacji:
- [ ] Zapisałeś liczbę zapytań i umiesz powiedzieć, skąd bierze się każde z nich: lista
  doktorów, konta, placówki.
- [ ] Umiesz powiedzieć, które z nich wynikają z domyślnego `EAGER`, a które z sięgania po
  kolekcję w pętli mapera.

## Jak sprawdzisz, że skończyłeś

- `\d doctor_facility` → dwie kolumny, `PRIMARY KEY` z obu
- `POST /doctors/1/facilities/1` → 200, drugi raz → 200 bez duplikatu
- `POST /doctors/1/facilities/2`, potem `DELETE /doctors/1/facilities/2` → 200, zostaje jeden wiersz
- `POST /doctors/1/facilities/999` → 404
- `mvn test` → `BUILD SUCCESS`

## Zadanie D1. Wszystkie relacje jawnie `LAZY`

Przejdź przez wszystkie encje i każdej relacji ustaw `fetch = FetchType.LAZY`: konto pacjenta i konto
doktora, doktor i pacjent wizyty, wizyty doktora, placówki doktora i doktorzy placówki.

Kryteria akceptacji:
- [ ] Nie ma w projekcie ani jednego `@ManyToOne`, `@OneToOne`, `@OneToMany` ani
  `@ManyToMany` bez `fetch = FetchType.LAZY`.
- [ ] Kolekcja Bruno z poprzednich lekcji przechodzi bez zmian (asercje tam, gdzie je dodałeś).

## Zadanie D2. N+1 na liście doktorów: zmierz i napraw

Po D1 powtórz pomiar z zadania 3 z zajęć, a potem napraw listę doktorów tak, żeby
`GET /doctors` wysyłał jedno zapytanie.

Kryteria akceptacji:
- [ ] Masz zanotowane trzy liczby zapytań dla tych samych danych: przed D1, po D1 i po
  naprawie.
- [ ] Naprawa to `@EntityGraph` na metodzie repozytorium (albo `JOIN FETCH` w `@Query`), a nie
  `EAGER` na relacji.
- [ ] Po naprawie `GET /doctors` wysyła jedno zapytanie `select` i zwraca e-maile oraz placówki
  doktorów.

## Zadanie D3. Placówki przez API i folder `placowki` w Bruno

Dopisz `/facilities`: `GET` (lista), `GET /{id}` (404, gdy nie ma), `POST` (201, 409 przy
zajętej nazwie). Potem załóż w kolekcji folder `placowki` ze scenariuszem z kompendium: placówka,
przypisanie doktora, lista placówek doktora.

Kryteria akceptacji:
- [ ] Folder ma co najmniej: utworzenie placówki (201), zajętą nazwę (409), przypisanie doktora
  numer 1 do nowej placówki (200), odczyt doktora z nową placówką na liście (200),
  przypisanie do nieistniejącej placówki (404).
- [ ] Każde żądanie ma asercję statusu, a odczyt doktora dodatkowo sprawdza, że
  `res.body.facilities` zawiera nazwę nowej placówki.
- [ ] Numer nowej placówki nie jest wpisany na sztywno: zapisz go w skrypcie po żądaniu
  (`bru.setVar("placowkaId", res.body.id);`) i użyj w ścieżce jako `{{placowkaId}}`.
- [ ] Po `docker compose down -v` folder przechodzi przy pierwszym przebiegu.

## Jak sprawdzisz, że skończyłeś

Skasuj wolumen, podnieś bazę, uruchom aplikację i puść folder:

```bash
cd bruno
npx @usebruno/cli@4.1.0 run placowki --env local
```

Asercje mają przejść (`Requests: 5 (5 Passed)`). W `psql` zapytanie o `doctor_facility`
złączone z doktorami i placówkami pokazuje przypisanie z folderu. `mvn test` przechodzi przy
zatrzymanym kontenerze.

## Lektury

- `baeldung.com/hibernate-many-to-many`: M:N z tabelą pośrednią, do sekcji o `Set` i `List`
  włącznie.
- Kompendium, rozdz. 22, s. 149-153. Kompendium poleca też wyłączyć `open-in-view`; my zostajemy
  przy ustawieniu domyślnym, bo mapujemy w kontrolerze (rozdział 9 materiału pokazuje, co
  się dzieje po wyłączeniu).

