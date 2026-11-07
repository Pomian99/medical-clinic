# Lekcja 24. Kaskady i pułapki relacji dwukierunkowych. Ćwiczenia

## Na zajęciach (30 min): MED-25

### Zadanie 1 (8 min). Rejestracja pacjenta jednym `save`

Dziś `PatientService.create` zapisuje najpierw konto, potem pacjenta.

1. W `PatientService.create` zbuduj konto, ale go nie zapisuj: wepnij je w pacjenta
   i zapisz samego pacjenta.
2. Uruchom i wyślij `POST /patients` z nowym pacjentem, na przykład (żądanie „Utwórz
   pacjenta" z Bruno wysyła Annę Nowak, która już jest w `data.sql`, więc dostałbyś 409):
   `{"email":"marta.lis@example.com","password":"silne321","idCardNo":"MNO444555",
   "firstName":"Marta","lastName":"Lis","phoneNumber":"500600704","birthday":"1995-03-08"}`.
   Zanotuj status i wyjątek z logu.
3. Dodaj przy `Patient.user` kaskadę, która sprawi, że zapis pacjenta spłynie na nowe konto.
   Tylko ten jeden typ kaskady.

Kryteria akceptacji:
- [ ] Bez kaskady `POST /patients` daje 500, a w logu jest `TransientPropertyValueException`.
- [ ] Z kaskadą to samo żądanie daje 201, a w logu są dwa wiersze w tej kolejności:
  `Hibernate: insert into users` i `Hibernate: insert into patients`.
- [ ] W `PatientService.create` nie ma już `userRepository.save`.

### Zadanie 2 (10 min). Test cyklu życia konta doktora

1. Dopisz na końcu `data.sql` pacjentkę na koncie doktor Katarzyny Wróbel (doktor, która leczy
   się u siebie): numer dowodu `KLM222333`, konto `katarzyna.wrobel@example.com`,
   `ON CONFLICT (user_id) DO NOTHING`.
2. Odpowiedz na pytanie: czy konto doktora ma prawo istnieć bez doktora? Dobierz kaskadę
   przy `Doctor.user` według odpowiedzi: zapis i scalanie mają spływać na konto, usunięcie nie.
   Powód zapisz w komentarzu nad polem.
3. W `psql` policz wiersze `users`, `patients`, `doctors`, potem `DELETE /doctors/1` i policz
   jeszcze raz.

Kryteria akceptacji:
- [ ] Przy `Doctor.user` jest `cascade = {CascadeType.PERSIST, CascadeType.MERGE}`, bez
  `REMOVE` i bez `ALL`, a komentarz podaje powód.
- [ ] `DELETE /doctors/1` → 204; w logu `delete from doctor_facility` i `delete from doctors`,
  nic więcej.
- [ ] Po usunięciu doktora konto `katarzyna.wrobel@example.com` i pacjentka `KLM222333` są
  w bazie.

### Zadanie 3 (12 min). Łańcuch domina na próbę

1. W `User` dodaj stronę odwrotną do pacjenta: `@OneToOne(mappedBy = "user", fetch =
   FetchType.LAZY, cascade = CascadeType.ALL)`. Uzasadnij to w komentarzu: pacjent bez
   konta w tej domenie nie istnieje.
2. Na próbę zmień kaskadę przy `Doctor.user` na `ALL`. Postaw czystą bazę, uruchom aplikację,
   policz wiersze, wyślij `DELETE /doctors/1` i policz jeszcze raz.
3. Wróć przy `Doctor.user` do `PERSIST` i `MERGE`.

Kryteria akceptacji:
- [ ] Masz zanotowane, co zniknęło po `DELETE /doctors/1` z `ALL` po obu stronach: kolejne
  `delete` z logu i liczby wierszy przed i po.
- [ ] Umiesz pokazać w encjach, którędy przeszła lawina.
- [ ] Po powrocie aplikacja się buduje, a `DELETE /doctors/1` znów zostawia konto i pacjentkę.

## Jak sprawdzisz, że skończyłeś

- `POST /patients` → 201, w logu `insert into users`, potem `insert into patients`
- przy `Doctor.user` jest `PERSIST` i `MERGE`, bez `ALL`
- `DELETE /doctors/1` → 204, konto i pacjentka `KLM222333` zostają
- `./mvnw test` → `BUILD SUCCESS`

## Podpowiedzi (czytaj dopiero, gdy utkniesz)

- Zadanie 1: `@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)` nad
  `Patient.user`. Import `jakarta.persistence.CascadeType`.
- Zadanie 2: wpis w `data.sql` wzoruj na pacjentach z początku pliku: `INSERT INTO patients
  (...) SELECT id, ... FROM users WHERE email = 'katarzyna.wrobel@example.com'`.
- Zadanie 2: liczenie w jednym zapytaniu: `SELECT (SELECT count(*) FROM users) AS users,
  (SELECT count(*) FROM patients) AS patients, (SELECT count(*) FROM doctors) AS doctors;`.
- Zadanie 3: po dodaniu pola do `User` maper konta przestaje się kompilować
  (`Unmapped target property: "patient"`). Przy `UserMapper.toEntity` dopisz
  `@Mapping(target = "patient", ignore = true)`.
- Zadanie 3: kolejność w logu czytaj od góry. Pierwszy `delete` dotyczy tabeli pośredniej:
  jej wiersze znikają z doktorem zawsze, bez żadnej kaskady.

## Zadanie D1. Discovery: audyt relacji dwukierunkowych

Przejdź przez wszystkie encje i spisz w pliku `docs/discovery-relacje.md` znaleziska w czterech
punktach poniżej. Przy każdym podaj: co znalazłeś, jak to sprawdziłeś, co zrobiłeś.

1. Rekurencja w JSON-ie: czy któryś kontroler zwraca encję zamiast DTO i przez które pole
   zamknęłaby się pętla.
2. Lombok na encjach z relacjami: `@Data` i `@ToString`.
3. Niespójność stron: miejsca, które zmieniają relację tylko po jednej stronie.
4. Kaskady w obie strony: tabela z każdą kaskadą w projekcie i jej testem cyklu życia.

Kryteria akceptacji:
- [ ] Plik ma cztery sekcje, po jednej na punkt, a każda podaje sposób sprawdzenia (polecenie,
  żądanie albo zapytanie).
- [ ] `grep -rnE '^ *@(Data|ToString)' src/main/java` nic nie znajduje.
- [ ] `grep -rn 'getFacilities().add' src/main/java` nic nie znajduje.

## Zadanie D2. Co znika po każdym `DELETE`

Dla `DELETE /doctors/{id}` i `DELETE /users/{id}` sprawdź w `psql`, co faktycznie znika.
Kompendium każe zmierzyć stan przed poprawką kaskad i po niej; stan „przed" (lawinę)
zmierzyłeś w zadaniu 3 na zajęciach, tu liczysz stan po poprawce.
Doktor ma mieć przypisaną placówkę, a na jego koncie ma stać pacjentka (stan z zadania 2
z zajęć).

Kryteria akceptacji:
- [ ] W sekcji 4 notatki są liczby wierszy `users`, `patients`, `doctors` przed i po każdym
  z dwóch żądań oraz kolejne `delete` z logu.
- [ ] `DELETE /doctors/1` → 204: znika doktor i jego wiersz w `doctor_facility`, konto zostaje.
- [ ] `DELETE /users/{id}` na koncie, na które wskazuje pacjentka, → 409, nic nie znika.

## Zadanie D3. Strony odwrotne: usuń albo obroń

Przy każdym polu z `mappedBy` sprawdź, czy jakikolwiek kod je czyta (nie tylko zapisuje). Pole
bez czytelnika usuń razem z jego `ignore` w maperze i z drugą linijką metody synchronizującej.
Pole, które zostaje, obroń w notatce: podaj kod, który po nim nawiguje.

Kryteria akceptacji:
- [ ] W sekcji 3 notatki jest tabela: pole, kto je czyta, decyzja.
- [ ] `grep -rn 'mappedBy' src/main/java` pokazuje tylko pola obronione w notatce.
- [ ] Aplikacja się buduje, `POST /patients` dalej daje 201, a `POST /doctors/1/facilities/1`
  dalej zwraca doktora z placówką.

## Jak sprawdzisz, że skończyłeś

Skasuj wolumen, podnieś bazę, uruchom aplikację i sprawdź:

```bash
grep -E '^## ' docs/discovery-relacje.md
curl -i -X POST localhost:8080/doctors/1/facilities/1
curl -i -X DELETE localhost:8080/doctors/1
curl -i -X DELETE localhost:8080/users/4
```

Nagłówki notatki to cztery punkty, usunięcie doktora zwraca 204, usunięcie konta 409.
`./mvnw test` przechodzi przy zatrzymanym kontenerze.

## Lektury

- `baeldung.com/jpa-cascade-types`: wszystkie typy kaskad z przykładami. Przeczytaj do sekcji
  3.5 włącznie. Dalej są typy Hibernate'a, które wystarczy rozpoznać.
