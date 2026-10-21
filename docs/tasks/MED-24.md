# Lekcja 21. Relacje: jeden do jednego. Ćwiczenia

## MED-24

### Zadanie 1 (12 min). Konto jako osobna encja

Pacjent i doktor potrzebują tego samego: e-maila, hasła i roli. Wyprowadź te pola do
własnej encji i połącz ją z pacjentem relacją jeden do jednego.

1. W pakiecie `model` utwórz enum `Role` z wartościami `PATIENT` i `DOCTOR`.
2. Utwórz encję `User` (tabela `users`): `id`, `email` (wymagany i unikalny), `password`
   (wymagane), `role` (wymagana, zapisywana nazwą).
3. Utwórz `UserRepository` z metodą `existsByEmail`.
4. W `Patient` usuń pola `email` i `password`, a w ich miejsce dodaj pole `user` typu `User`
   z relacją jeden do jednego i kolumną klucza obcego `user_id`, wymaganą i unikalną.
5. Przepisz `data.sql`: najpierw trzy konta z rolą `PATIENT`, potem trzech pacjentów, którzy
   biorą identyfikator konta zapytaniem po e-mailu.

Kryteria akceptacji:
- [ ] W `Patient` nie ma pól `email` ani `password`, jest pole `User user`.
- [ ] Pole `user` ma `@JoinColumn` z `nullable = false` i `unique = true`.
- [ ] `data.sql` wstawia konta przed pacjentami, a każdy `INSERT` ma
  `ON CONFLICT ... DO NOTHING`. To, że skrypt przechodzi dwa razy z rzędu, sprawdzisz
  po zadaniu 2, uruchamiając aplikację drugi raz na tej samej bazie.

### Zadanie 2 (12 min). Reszta projektu idzie za e-mailem

E-mail mieszka teraz w koncie, więc repozytorium, maper, serwis i klasa aktualizacji muszą
to uwzględnić. Rejestracja pacjenta (`POST /patients`) ma dalej przyjmować e-mail i hasło
i zakładać oba wiersze jednym żądaniem.

1. W `PatientRepository` zamień `findByEmail` na wyszukiwanie przez relację, a
   `existsByEmail` usuń (jest już w `UserRepository`).
2. Z `PatientUpdateCommand` usuń e-mail: login zmienia się w koncie, nie w danych pacjenta.
3. W `PatientMapper` weź e-mail do `PatientDto` z konta, a przy mapowaniu z Commanda na encję
   (i przy aktualizacji) pomiń pole `user`. W metodzie `update` usuń ignorowanie `password`,
   bo tego pola w `Patient` już nie ma.
4. W `PatientService.create` sprawdź zajęty e-mail w repozytorium kont, zapisz konto
   z rolą `PATIENT`, podepnij je pod pacjenta i zapisz pacjenta. `changePassword` ma
   zmieniać hasło w koncie.
5. Skasuj wolumen, podnieś bazę, zbuduj i uruchom.

Kryteria akceptacji:
- [ ] `mvn clean package` kończy się na `BUILD SUCCESS`.
- [ ] `GET /patients` zwraca trzech pacjentów z e-mailami, choć tabela `patients` nie ma
  już kolumny `email`.
- [ ] `GET /patients?email=jan.wisniewski@example.com` → 200 i Jan Wiśniewski.
- [ ] `POST /patients` z nowym e-mailem → 201, a w logu widać dwa `INSERT`-y: najpierw
  `insert into users`, potem `insert into patients`.
- [ ] `POST /patients` z tym samym e-mailem drugi raz → 409.
- [ ] Po zatrzymaniu i ponownym uruchomieniu aplikacji (bez `down -v`) start przechodzi bez
  błędu, czyli `data.sql` wykonuje się drugi raz na tych samych danych.

### Zadanie 3 (6 min). Sprawdź w bazie, kto pilnuje relacji

Zajrzyj do `psql` i zobacz, co naprawdę powstało z twoich adnotacji.

```bash
docker compose exec postgres psql -U clinic -d clinic -c "\d patients"
```

Potem spróbuj prosto w SQL-u podpiąć drugiego pacjenta pod konto numer 1.

```bash
docker compose exec postgres psql -U clinic -d clinic -c "INSERT INTO patients (user_id, id_card_no, first_name, last_name) VALUES (1, 'KLM000111', 'Adam', 'Kos');"
```

Na koniec zmień hasło pacjenta 1 (`PATCH /patients/1/password`) i sprawdź w logu, do której
tabeli idzie `update`.

Kryteria akceptacji:
- [ ] W `\d patients` widzisz kolumnę `user_id`, ograniczenie `UNIQUE CONSTRAINT` na niej
  i klucz obcy `REFERENCES users(id)`.
- [ ] `INSERT` drugiego pacjenta na konto 1 kończy się `duplicate key value violates unique
      constraint`.
- [ ] `PATCH /patients/1/password` → 200, a w logu widać `update users set ...`.
- [ ] Umiesz powiedzieć, która encja jest właścicielem relacji i po czym to poznajesz.

## Jak sprawdzisz, że skończyłeś

- `docker compose down -v`, `docker compose up -d --wait`, uruchomienie aplikacji → startuje
  bez błędu
- `GET /patients` → 200, trzech pacjentów z e-mailami
- `GET /patients?email=...` → 200
- `POST /patients` z nowym e-mailem → 201 i dwa `INSERT`-y w logu
- `POST /patients` z zajętym e-mailem → 409
- `mvn test` → `BUILD SUCCESS`

## Zadanie D1. Konto jako osobny zasób

Za tydzień do kliniki dojdzie doktor. Konto doktora zakłada recepcja, a rolę doktora nadaje
się potem, więc konto musi dać się utworzyć samodzielnie, bez pacjenta. Wystaw konta jako
zasób `/users` przez wszystkie warstwy: Command, DTO, maper, serwis, kontroler.

Kryteria akceptacji:
- [ ] `POST /users` z e-mailem, hasłem i rolą → 201, w odpowiedzi `id`, `email` i `role`.
- [ ] Odpowiedź nigdy nie zawiera hasła: ani przy tworzeniu, ani na liście.
- [ ] `POST /users` z e-mailem, który ma już jakiekolwiek konto → 409.
- [ ] `POST /users` bez roli albo z hasłem krótszym niż 8 znaków → 400 z listą złamanych
  reguł (handler z lekcji 14).
- [ ] `GET /users` → 200 i lista kont, `GET /users/{id}` → 200, `GET /users/999` → 404.

## Zadanie D2. Konta, którego ktoś używa, nie wolno skasować

Dopisz `DELETE /users/{id}`. Konto bez pacjenta można skasować, konta pacjenta nie.
Zanim napiszesz regułę, sprawdź, co się dzieje bez niej: baza i tak odmówi, bo tabela
`patients` wskazuje konto kluczem obcym. Pytanie brzmi, jaki status dostanie klient.

Kryteria akceptacji:
- [ ] `DELETE /users/{id}` konta bez pacjenta → 204, a potem `GET` tego konta → 404.
- [ ] `DELETE /users/999` → 404.
- [ ] `DELETE /users/1` (konto pacjenta) → 409 z czytelnym komunikatem, a nie 500.
- [ ] Pytanie „czy jakiś pacjent wskazuje to konto" to jedna metoda w `PatientRepository`,
  zapytanie z nazwy metody, bez `@Query`.

## Zadanie D3. Folder `konta` w kolekcji Bruno

Dopisz do kolekcji kliniki folder `konta` (nie `users`: taki folder jest już w kolekcji
od lekcji 3 i wysyła żądania do publicznego API testowego). Potem sprawdź w bazie, które
konta nie mają pacjenta.

Kryteria akceptacji:
- [ ] Folder ma co najmniej sześć żądań: utworzenie konta, lista, konto po id, zajęty
  e-mail (409), kasowanie konta pacjenta (409) i nieistniejące konto (404).
- [ ] Na świeżej bazie cały folder przechodzi za pierwszym uruchomieniem.
- [ ] Zapytaniem w `psql` (złączenie `users` z `patients` i warunek na brak pacjenta)
  pokazujesz konta, których nie wskazuje żaden pacjent.

## Jak sprawdzisz, że skończyłeś

Skasuj wolumen (`docker compose down -v`), podnieś bazę i uruchom aplikację. Uruchom folder
`konta` z terminala:

```bash
cd bruno && npx @usebruno/cli@4.1.0 run konta --env local
```

Wszystkie żądania mają przejść. Potem uruchom całą kolekcję pacjentów: rejestracja,
wyszukiwanie po e-mailu i zmiana hasła działają tak samo jak przed dzisiejszą lekcją,
choć e-mail i hasło leżą w innej tabeli. `mvn test` przechodzi przy zatrzymanym kontenerze.

## Lektury

- `baeldung.com/jpa-one-to-one`: trzy strategie mapowania 1:1 z przykładami, do sekcji
  o tabeli łączącej włącznie.
