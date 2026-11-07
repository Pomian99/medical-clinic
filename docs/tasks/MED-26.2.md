### Zadanie 1 (10 min). Wersja wiersza w wizycie

1. Dodaj do encji `Visit` pole `version` typu `Long` z adnotacją `@Version`
   (`jakarta.persistence.Version`).
2. Zbuduj projekt. Gdy kompilacja padnie, popraw maper wizyt tak, żeby `toEntity` nie próbował
   ustawiać wersji.
3. Wyczyść bazę (`docker compose down -v`, `docker compose up -d --wait`) i uruchom aplikację.

Kryteria akceptacji:
- [ ] `./mvnw -q -DskipTests package` przechodzi.
- [ ] `"\d visits"` w `psql` pokazuje kolumnę `version` typu `bigint` z `not null`.
- [ ] Po założeniu slotu wizyta ma `version` równe 0, a po rezerwacji 1 (`VisitDto` nie pokazuje
  wersji, więc sprawdź w `psql`:
  `SELECT id, patient_id, version FROM visits WHERE id = <id z odpowiedzi POST>;`).

### Zadanie 2 (8 min). Konflikt wersji jako 409

1. W `GlobalExceptionHandler` dodaj metodę, która tłumaczy
   `ObjectOptimisticLockingFailureException` na 409.
2. Napisz komunikat po angielsku, który mówi klientowi, co zrobić: pobrać zasób jeszcze raz
   i spróbować ponownie.
3. W logu zostaw ostrzeżenie z nazwą klasy encji.

Kryteria akceptacji:
- [ ] Handler zwraca `ProblemDetail` ze statusem 409.
- [ ] Wyjątek łapie osobna metoda, a nie siatka bezpieczeństwa `Exception.class`.

### Zadanie 3 (12 min). Dwie rezerwacje naraz

1. Na czystej bazie, przy działającej aplikacji, zapisz w katalogu projektu plik `wyscig.sh`
   z pętlą poniżej i uruchom `bash wyscig.sh`. Pętla zakłada 20 slotów doktora 1 i na każdy
   wysyła naraz dwie rezerwacje (pacjent 1 i pacjent 2).
2. Porównaj wynik z wersją bez handlera z zadania 2 (zakomentuj metodę, przebuduj, wyczyść bazę).

```bash
for i in $(seq 1 20); do
  d=$(printf '2030-06-%02d' "$i")
  curl -s -o /dev/null -X POST localhost:8080/visits -H 'Content-Type: application/json' \
       -d "{\"doctorId\":1,\"startsAt\":\"${d}T10:00:00\",\"endsAt\":\"${d}T10:30:00\"}"
done
for id in $(seq 1 20); do
  curl -s -o /dev/null -w '%{http_code}\n' -X PATCH localhost:8080/visits/$id/book/1 &
  curl -s -o /dev/null -w '%{http_code}\n' -X PATCH localhost:8080/visits/$id/book/2 &
  wait
done | sort | uniq -c
```

Kryteria akceptacji:
- [ ] Z handlerem: 20 odpowiedzi 200 i 20 odpowiedzi 409.
- [ ] Bez handlera: 20 odpowiedzi 200 i 20 odpowiedzi 500.
- [ ] W logu są ostrzeżenia `konflikt wersji` z nazwą klasy `Visit`.
- [ ] Umiesz powiedzieć, co dostawał drugi pacjent przed dodaniem `@Version`.

## Jak sprawdzisz, że skończyłeś

Na czystej bazie (`docker compose down -v`, `docker compose up -d --wait`, start aplikacji):

- `bash wyscig.sh` → `20 200` i `20 409`
- nowy slot → `version` 0, rezerwacja → `version` 1
- `./mvnw test` → `BUILD SUCCESS`

## Podpowiedzi (czytaj dopiero, gdy utkniesz)

- Zadanie 1: błąd `Unmapped target property: "version"` znaczy, że maper ma politykę ERROR.
  W `toEntity` dopisz `@Mapping(target = "version", ignore = true)`.
- Zadanie 1: jeśli aplikacja wstaje, a każde żądanie o wizyty daje 500 z `column ... version
  does not exist` w logu, w bazie zostały stare wizyty. Kolumna `not null` nie powstała;
  wyczyść bazę.
- Zadanie 2: klasa leży w pakiecie `org.springframework.orm`.
- Zadanie 3: jeśli wyszło `19 200` i `21 409`, jedna z wizyt 1-20 była zarezerwowana wcześniej.
  Puść pętlę po `docker compose down -v`.
- Zadanie 3: pojedyncze 409 „already booked" (na przykład `19 500` i `1 409` bez handlera)
  znaczą, że para żądań się nie zazębiła. Suma rezerwacji w bazie i tak wynosi 20.

## Lektury

- `baeldung.com/spring-transactional-propagation-isolation`: ściągawka z propagacji i izolacji.
  Przeczytaj całość i wracaj do niej.
