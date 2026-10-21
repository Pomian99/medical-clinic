# Lekcja 25. Logika biznesowa na danych: sloty wizyt. Ćwiczenia

## Na zajęciach (30 min): MED-19 część 2

### Zadanie 1 (10 min). Reguły bez bazy

1. Utwórz wyjątek `InvalidVisitException` ze statusem 400.
2. W `VisitService.create`, zanim sięgniesz do bazy, sprawdź po kolei: start nie jest
   w przeszłości, start wypada na pełnym kwadransie (bez sekund), a długość
   `endsAt - startsAt` jest dodatnią wielokrotnością 15 minut. Każda złamana reguła rzuca
   `InvalidVisitException` z komunikatem, który mówi, co poprawić.

Kryteria akceptacji:
- [ ] `w-2020.json`, `w-1005-1035.json` i `w-1000-1020.json` dają 400, każdy z innym `detail`.
- [ ] `w-1000-1030.json` daje 201.
- [ ] Reguły stoją w serwisie przed `doctorRepository.findById`.

### Zadanie 2 (12 min). Nakładanie jednym zapytaniem

1. W `VisitRepository` dopisz metodę, której nazwa buduje zapytanie i która odpowiada, czy doktor ma wizytę
   nachodzącą na podany przedział: istniejąca wizyta zaczyna się przed końcem nowej
   i kończy po jej początku.
2. Utwórz `VisitOverlapsException` ze statusem 409 i rzuć go w `VisitService.create`, gdy
   metoda zwróci `true`.
3. Dopisz 409 do dokumentacji `POST /visits` w kontrolerze.
4. Przed sprawdzeniem wyczyść bazę (`docker compose down -v`, `docker compose up -d --wait`),
   bo slot 10:00-10:30 z zadania 1 dalej w niej stoi, i uruchom aplikację.

Kryteria akceptacji:
- [ ] Po `w-1000-1030.json` żądanie `w-1015-1045.json` daje 409, a `w-1030-1100.json` 201.
- [ ] W logu zapytanie ma ostre nierówności: `starts_at<?` i `ends_at>?` (z aliasem tabeli
  przed kolumną, np. `v1_0.starts_at<?`).
- [ ] Umiesz wskazać, który argument metody porównujesz z którą kolumną.

### Zadanie 3 (8 min). Zegar jako bean

1. Utwórz klasę `ClockConfig` z `@Configuration` i beanem `Clock`
   (`Clock.systemDefaultZone()`).
2. Wstrzyknij `Clock` do `VisitService` i sprawdzaj przeszłość przez
   `LocalDateTime.now(clock)`.
3. Przesuń daty w folderze `wizyty` w Bruno na 14 stycznia 2030. Daty z października 2026
   złamią regułę przeszłości, gdy miną, a 7 stycznia zajmują twoje sloty z zadań 1-2.

Kryteria akceptacji:
- [ ] `grep -rn 'now(' src/main/java` pokazuje w kodzie tylko `LocalDateTime.now(clock)`
  (nie licząc komentarzy).
- [ ] Folder `wizyty` w Bruno przechodzi (4 z 4) przy pierwszym przebiegu po
  `docker compose down -v`.

## Jak sprawdzisz, że skończyłeś

Na czystej bazie (`docker compose down -v`, `docker compose up -d --wait`, start aplikacji):

- trzy złe sloty → 400, każdy z innym `detail`
- `w-1000-1030.json` → 201, `w-1015-1045.json` → 409, `w-1030-1100.json` → 201
- `./mvnw test` → `BUILD SUCCESS`

## Podpowiedzi (czytaj dopiero, gdy utkniesz)

- Zadanie 1: minuty `start.getMinute() % 15`, a sekundy i nanosekundy mają być zerem.
  Długość: `Duration.between(start, end)`; `toMinutes()` obetnie sekundy, więc resztę
  z dzielenia przez kwadrans licz w nanosekundach.
- Zadanie 2: `existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(doctorId, end, start)`.
  Kolejność argumentów idzie za kolejnością warunków w nazwie: `StartsAtBefore` dostaje
  koniec nowej wizyty.
- Zadanie 2: `Before` i `After` to ostre nierówności. `LessThanEqual` zrobiłby ze styku
  końców konflikt.
- Zadanie 3: serwis z `@RequiredArgsConstructor` dostanie zegar, gdy dopiszesz pole
  `private final Clock clock;`.

## Zadanie D1. Rezerwacja slotu

Dopisz `PATCH /visits/{id}/book/{patientId}`, który zapisuje pacjenta na wolny slot i zwraca
wizytę.

Kryteria akceptacji:
- [ ] Wolny slot w przyszłości → 200, w odpowiedzi `patientId` pacjenta.
- [ ] Wizyta albo pacjent, których nie ma → 404.
- [ ] Slot, który ma już pacjenta → 409.
- [ ] Slot z przeszłości → 400. Przeszłość sprawdzasz tym samym wstrzykniętym zegarem co przy
  zakładaniu slotu.
- [ ] O tym, czy slot jest wolny, rozstrzyga metoda wizyty, a nie `== null` w serwisie.

## Zadanie D2. Wizyty pacjenta

Dopisz `GET /visits/patient/{patientId}`, który zwraca wizyty pacjenta jako `VisitDto`.

Kryteria akceptacji:
- [ ] Po rezerwacji z D1 lista pacjenta ma tę wizytę, z `doctorId` i `patientId`.
- [ ] Pacjent bez wizyt dostaje pustą listę.

## Zadanie D3. Folder `sloty` w Bruno

Załóż folder `sloty` ze scenariuszem z kompendium: slot, rezerwacja, konflikt nakładania,
konflikt podwójnej rezerwacji.

Kryteria akceptacji:
- [ ] Cztery żądania w tej kolejności: nowy slot (201), rezerwacja dla pacjenta 1 (200), slot
  nachodzący na zarezerwowany (409), rezerwacja tego samego slotu dla pacjenta 2 (409).
- [ ] Każde żądanie ma asercję statusu, a rezerwacja sprawdza też `res.body.patientId`.
- [ ] Numer slotu nie jest wpisany na sztywno: `bru.setVar("slotId", res.body.id);`
  w skrypcie po pierwszym żądaniu i `{{slotId}}` w ścieżkach.
- [ ] Po `docker compose down -v` folder przechodzi przy pierwszym przebiegu.

## Jak sprawdzisz, że skończyłeś

Skasuj wolumen, podnieś bazę, uruchom aplikację i puść folder:

```bash
cd bruno
npx @usebruno/cli@4.1.0 run sloty --env local
```

Asercje mają przejść (`Requests: 4 (4 Passed)`). Slot z przeszłości, na którym sprawdzisz 400,
wstaw przez `psql` (`INSERT INTO visits (doctor_id, starts_at, ends_at) VALUES (1, TIMESTAMP
'2020-01-06 10:00', TIMESTAMP '2020-01-06 10:30') RETURNING id;`), bo API takiego slotu nie
założy. `./mvnw test` przechodzi przy zatrzymanym kontenerze.

## Lektury

- `baeldung.com/java-check-two-date-ranges-overlap`: wyprowadzenie warunku nakładania
  z wariantami. Krótki tekst, przeczytaj całość.
