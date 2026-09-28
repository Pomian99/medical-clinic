### Zadanie 1 (10 min). Lista pacjentów
Utwórz pakiet `controller`, a w nim klasę `PatientController`. Oznacz klasę tak, żeby
Spring traktował ją jako kontroler odbierający żądania pod `/patients`. Wstrzyknij `PatientService`
konstruktorem (tak jak w serwisie w lekcji 7). Dodaj metodę obsługującą `GET /patients`,
która zwraca listę wszystkich pacjentów z serwisu.

Kryteria akceptacji:
- [ ] `GET /patients` z Bruno zwraca status 200 i `[]` (lista jest jeszcze pusta).
- [ ] W klasie nie ma słowa `new` ani logiki; kontroler tylko woła serwis.
- [ ] Żądanie „lista pacjentów" jest zapisane w kolekcji Bruno w folderze `patients`.

### Zadanie 2 (10 min). Dodawanie pacjenta
Dodaj metodę obsługującą `POST /patients`. Body żądania (JSON) ma zamienić się w obiekt
`Patient` i trafić do metody `create` serwisu. Odpowiedź ma mieć status 201 i zawierać
zapisanego pacjenta z nadanym `id`.

Wyślij z Bruno to body:

```json
{
  "email": "anna.nowak@example.com",
  "password": "tajne123",
  "idCardNo": "ABC123456",
  "firstName": "Anna",
  "lastName": "Nowak",
  "phoneNumber": "600100200",
  "birthday": "1990-05-12"
}
```

Kryteria akceptacji:
- [ ] `POST /patients` zwraca 201, a w odpowiedzi jest `"id": 1` i data `"1990-05-12"`
  w tej samej postaci, w jakiej ją wysłałeś.
- [ ] `GET /patients` zwraca teraz listę z jednym pacjentem.
- [ ] Wysłanie zepsutego JSON-a (usuń cudzysłów przy `email`) daje 400, a aplikacja
  dalej działa.
- [ ] Oba żądania są w kolekcji Bruno.

### Zadanie 3 (10 min). Jeden pacjent po id
Dodaj metodę obsługującą `GET /patients/{id}`. Gdy pacjent istnieje, odpowiedź to 200
i jego dane. Gdy nie istnieje, odpowiedź to 404 bez body. Skorzystaj z tego, że
`PatientService.findById` zwraca `Optional<Patient>`. Po restarcie lista w pamięci jest
pusta, więc zanim sprawdzisz kryteria, wyślij jeszcze raz `POST` z Anną z zadania 2.

Kryteria akceptacji:
- [ ] `GET /patients/1` zwraca 200 i dane Anny.
- [ ] `GET /patients/99` zwraca 404.
- [ ] `GET /patients/abc` zwraca 400 (tego nie piszesz; sprawdź tylko, że tak jest).
- [ ] Żądanie `GET {{baseUrl}}/patients/1` jest w kolekcji Bruno.

Jeśli skończysz wcześniej: wyślij dwa razy tego samego pacjenta i zobacz, jaki status
dostajesz. Zapisz go sobie; wrócimy do tego w pracy domowej.

## MED-03, część 2: dokończ CRUD pacjentów

### Zadanie D1. Pełna aktualizacja pacjenta
Dodaj `PUT /patients/{id}`. Body ma ten sam kształt co przy tworzeniu. Serwis dostaje
`id` i nowe dane, podmienia wszystkie pola istniejącego pacjenta i zwraca go; gdy
pacjenta nie ma, kontroler odpowiada 404.
Po restarcie lista jest pusta, więc zanim sprawdzisz kryteria zadań 1 i 2, wyślij `POST`
z Anną z ćwiczeń na zajęciach (dostanie `id` równe 1).

Kryteria akceptacji:
- [ ] `PUT /patients/1` z nowym nazwiskiem zwraca 200 i pacjenta z nowym nazwiskiem.
- [ ] `GET /patients/1` pokazuje zmienione dane.
- [ ] `PUT /patients/99` zwraca 404.

### Zadanie D2. Usuwanie pacjenta
Dodaj `DELETE /patients/{id}`. Gdy pacjent istniał i został usunięty, odpowiedź to 204
bez body. Gdy nie istniał, 404. Anna musi być na liście (patrz uwaga o restarcie w zadaniu 1). Repozytorium z lekcji 7 ma już metodę `deleteById(Long)`,
która zwraca `boolean` (czy coś usunięto); użyj jej w nowej metodzie serwisu.

Kryteria akceptacji:
- [ ] `DELETE /patients/1` zwraca 204, a `GET /patients` nie zawiera już Anny.
- [ ] Drugie `DELETE /patients/1` zwraca 404.

### Zadanie D3. Duplikat e-maila ma dostać 409
Serwis rzuca `PatientAlreadyExistsException`, gdy e-mail jest zajęty, a klient dostaje 500.
To nasz błąd, nie klienta. Oznacz klasę wyjątku adnotacją `@ResponseStatus(HttpStatus.CONFLICT)`.
Od tej chwili Spring sam zamienia ten wyjątek na 409. Docelową obsługę błędów zrobimy
w lekcji 14, dziś wystarczy ta jedna adnotacja.

Kryteria akceptacji:
- [ ] Drugi `POST /patients` z tym samym e-mailem zwraca 409.
- [ ] Pierwszy `POST` nadal zwraca 201.

### Zadanie D4. Kolekcja Bruno i data
- [ ] W folderze `patients` kolekcji są wszystkie żądania: lista, jeden po id, dodanie,
  pełna aktualizacja, usunięcie; każde wysłane i działające na środowisku `local`.
- [ ] Data urodzenia w odpowiedziach ma format ISO (`"1990-05-12"`), bez konfiguracji.
  Zanotuj w jednym zdaniu, dlaczego nie musiałeś nic ustawiać (rozdział 4 materiału
  do lekcji 8).
- [ ] Commit w Twoim repo z opisem zaczynającym się od `MED-03`.

Lektury z kompendium na następne zajęcia: „Spring Boot: Query Parameter vs Path Variable"
(Daryl Goh, medium.com) oraz tomaytotomato.com/overloading-rest-endpoints-on-spring-boot-2.
Zaczniemy od pytań do nich.


## Jak sprawdzisz, że skończyłeś
- `POST /patients` (nowy e-mail) → 201 · `POST /patients` (ten sam e-mail) → 409
- `PUT /patients/1` → 200 z nowymi danymi · `PUT /patients/99` → 404
- `DELETE /patients/1` → 204 · drugi raz → 404
- `GET /patients` → 200 i lista bez usuniętego pacjenta

## Podpowiedzi (czytaj dopiero, gdy utkniesz)
- Zadanie 1: metoda w serwisie może zwracać `Optional<Patient>` i użyć `map` na wyniku
  `findById`, żeby ustawić pola tylko wtedy, gdy pacjent istnieje. Kontroler mapuje `Optional`
  tak samo jak w `GET /patients/{id}`.
- Zadanie 2: `ResponseEntity<Void>` i wyrażenie warunkowe z `noContent()` albo `notFound()`.
  Repozytorium z lekcji 7 ma już `deleteById(Long)` zwracające `boolean`.
- Zadanie 3: adnotacja idzie na klasę wyjątku, nie na metodę kontrolera. Import
  z `org.springframework.web.bind.annotation`.
- Po każdej zmianie kodu zrestartuj aplikację; po restarcie lista jest pusta, więc zanim
  sprawdzisz PUT i DELETE, wyślij POST z Anną (dostanie `id` równe 1).
