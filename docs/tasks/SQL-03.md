## Zadanie 1 (12 min). Interfejs zamiast listy w pamięci

Napisz `PatientRepository`: interfejs w pakiecie `repository`, który dziedziczy po
`JpaRepository<Patient, Long>` i deklaruje jedną metodę: `findByEmail`. Przełącz
`PatientService` na to repozytorium i skasuj `InMemoryPatientRepository`. Metoda
`deleteById` z `JpaRepository` nic nie zwraca, więc sprawdź osobno, czy taki pacjent
istnieje.

Kryteria akceptacji:
- [ ] W projekcie nie ma już klasy `InMemoryPatientRepository`.
- [ ] `mvn clean package` kończy się na `BUILD SUCCESS`.
- [ ] `GET /patients` zwraca pacjentów z `data.sql`, a nie pustą tablicę.
- [ ] W logu aplikacji widać `Hibernate: select ... from patients ...`.
- [ ] `DELETE /patients/999` zwraca 404, a nie 204.

## Zadanie 2 (10 min). Zapis, o którym łatwo zapomnieć

Zmień nazwisko pacjenta przez `PUT /patients/1` i sprawdź w `psql`, czy zmiana jest
w tabeli. Jeżeli nie ma, popraw serwis tak, żeby była.

Kryteria akceptacji:
- [ ] Po `PUT` w tabeli `patients` jest nowe nazwisko, nie stare.
- [ ] W logu widać `Hibernate: update patients ...`.
- [ ] To samo działa dla zmiany hasła (`PATCH /patients/1/password`).
- [ ] Umiesz powiedzieć, czemu bez wywołania `save` zmiana ginie.

## Zadanie 3 (8 min). Kolekcja Bruno na trwałej bazie

Przejdź kolekcję kliniki i zobacz, co zmieniło się teraz, gdy dane nie znikają razem
z aplikacją. Uwaga na dwie rzeczy, które zachowują się inaczej niż tydzień temu:

1. Żądanie „Utwórz pacjenta" wysyła e-mail i numer dowodu, które są już w `data.sql`,
   więc dostaniesz 409 zamiast 201. Zmień w nim **oba** te pola (e-mail i `idCardNo`),
   zanim wyślesz.
2. Żądanie „Usuń pacjenta" kasuje wiersz na stałe. Po nim żądania po `id` dają 404, a po
   `docker compose down -v` i restarcie dane wracają z nowymi identyfikatorami, bo licznik
   klucza idzie dalej.

Kryteria akceptacji:
- [ ] `GET /patients` → 200, `GET /patients/{id}` → 200, `GET /patients?email=...` → 200.
- [ ] Twoje poprawione „Utwórz pacjenta" → 201, a wysłane drugi raz → 409.
- [ ] Żądania z celowym błędem nadal dają 400 i 404, tak jak w lekcji 14.
- [ ] Umiesz powiedzieć, czemu po `DELETE` żądanie po tym samym `id` daje teraz 404
  także po restarcie aplikacji.

## Jak sprawdzisz, że skończyłeś

- `GET /patients` → 200 i pacjenci z bazy
- log aplikacji → `Hibernate: select ... from patients`
- `PUT /patients/1` → 200, a w `psql` nowe nazwisko
- `DELETE /patients/999` → 404

## Zadanie D1. Jedno pytanie zamiast całego wiersza

Serwis sprawdza duplikat e-maila, pobierając cały wiersz i budując z niego obiekt,
po to tylko, żeby stwierdzić, czy ten e-mail jest już zajęty. Zamień to na metodę, która pyta wprost.

Kryteria akceptacji:
- [ ] `PatientRepository` ma metodę `existsByEmail` zwracającą `boolean`.
- [ ] Serwis używa jej przy zakładaniu pacjenta, a `findByEmail` zostaje tam, gdzie
  naprawdę potrzebny jest cały pacjent.
- [ ] `POST` z zajętym e-mailem nadal zwraca 409 z tym samym komunikatem.
- [ ] W logu przy tym żądaniu widać zapytanie po jednym wierszu, a nie pobranie
  wszystkich kolumn pacjenta.

## Zadanie D2. Zapytanie, którego nie da się nazwać

Dopisz wyszukiwanie pacjentów po fragmencie imienia albo nazwiska, bez względu na
wielkość liter. Nazwa metody dla takiego warunku byłaby nie do przeczytania, więc napisz
zapytanie wprost w JPQL przez `@Query`. Wystaw je jako kolejny wariant ścieżki
`GET /patients?fragment=...`.

Kryteria akceptacji:
- [ ] `GET /patients?fragment=kow` zwraca 200 i pacjentów, których imię albo nazwisko
  zawiera ten fragment.
- [ ] Wielkość liter nie ma znaczenia: `KOW` i `kow` dają ten sam wynik.
- [ ] Zapytanie w `@Query` operuje na encji `Patient` i nazwach pól, a nie na tabeli
  `patients` i nazwach kolumn.
- [ ] Parametr jest nazwany (`:fragment`), a nie pozycyjny.

## Zadanie D3. Kolekcja Bruno rośnie o jedno żądanie

Dopisz do kolekcji kliniki żądanie wyszukiwania po fragmencie nazwiska. Potem skasuj
wolumen (`docker compose down -v`), podnieś bazę i przejdź całą kolekcję od początku,
na świeżych danych startowych.

Kryteria akceptacji:
- [ ] W kolekcji jest żądanie `GET /patients?fragment=...` z sensowną nazwą po polsku.
- [ ] Na świeżej bazie cała kolekcja przechodzi: 200, 201, 409, 400 i 404 tam, gdzie
  było tak w lekcji 14.
- [ ] Żądanie „Utwórz pacjenta" ma dane, których nie ma w `data.sql` (inny e-mail
  i inny numer dowodu), więc za pierwszym razem daje 201.
- [ ] `mvn test` przechodzi przy zatrzymanym kontenerze bazy.

## Jak sprawdzisz, że skończyłeś

Zatrzymaj aplikację, skasuj wolumen (`docker compose down -v`), podnieś bazę i uruchom
wszystko od nowa. Dane startowe wejdą z `data.sql`, a cała kolekcja Bruno ma przejść tak
samo jak przed restartem. Jeżeli coś działa tylko za pierwszym razem, znaczy to, że
gdzieś jeszcze trzymasz stan w pamięci.

## Lektury

- `docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html`, rozdział o tworzeniu
  zapytań z nazw metod. Przeczytaj tabelę słów kluczowych: to jest cały mini-język.
- Kompendium, rozdz. 20, s. 136-141. Pamiętaj, że serwis z kompendium ma
  `@Transactional` (lekcja 26), którego u nas jeszcze nie ma, więc zapis wołasz jawnie.
