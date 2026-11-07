## MED-28 cz. 2: biała lista sortowania

Przed startem: baza i aplikacja jak w L34, kilkanaście wizyt doktora 1 (pętla z L34). Lista pól
do sortowania pochodzi z `docs/paginacja/plan-get-visits.md`.

**L35.1. Co dziś przechodzi** (przed jakąkolwiek zmianą)
- Wyślij `GET /visits?sort=abrakadabra`, `sort=patient.user.password`, `sort=patient.user.pesel`
  i zapisz statusy w komentarzu w planie.
- Umiesz powiedzieć, czemu pole z hasłem przechodzi, a pole, którego nie ma, daje 500, i czemu
  500 za literówkę to wina serwera, a nie klienta.

**L35.2. Biała lista w serwisie**
- Wyjątek `InvalidSortPropertyException` (dziedziczy po `MedicalClinicException`, status 400).
- `VisitService.findAll` sprawdza każde pole z `pageable.getSort()`, zanim zawoła repozytorium.
- Dozwolone pola jako `List.of(...)`, nie `Set.of(...)`.
- Komunikat nazywa odrzucone pole i wymienia dozwolone: `cannot sort by 'abrakadabra', allowed: startsAt, endsAt`.
- Bez nowego handlera w advice: wyjątek domenowy obsługuje piętro pierwsze.

**L35.3. Sprawdź**
- `sort=abrakadabra` i `sort=patient.user.password` → 400 z listą dozwolonych pól.
- `sort=startsAt,desc` → 200, wizyty od najpóźniejszej.
- Przy odrzuconym sortowaniu w logu nie ma `select ... from visits`.

**L35.4. Testy serwisu** (`VisitServiceTest`, dwa testy `findAll`)
- Pole z listy: serwis oddaje tę samą instancję `Page` co repozytorium (`assertSame`).
- Pole spoza listy: `assertThrows` z typem i pełnym komunikatem +
  `verify(visitRepository, never()).findAll(any(PageRequest.class))`.
- `Pageable` przez `PageRequest.of(...)` z `Sort.by(...)`.

**L35.5. Kolekcja Bruno** `bruno/wizyty-strony`, pięć żądań z asercjami
- Strona 0 i strona 1 po 5: 200, `res.body.page` i `res.body.size` zgodne z żądaniem.
- Sortowanie po `startsAt` rosnąco i malejąco: 200 i test w bloku `tests` na kolejność `startsAt` w `content`.
- Zły sort (`patient.user.password`): 400, `detail` zawiera listę dozwolonych pól.
- Zobacz porażkę asercji: zakomentuj sprawdzanie białej listy, przebuduj, zrestartuj, folder
  czerwony; przywróć, folder zielony.

Gotowe, gdy:
- `GET /visits?sort=abrakadabra` → 400, w `detail` lista dozwolonych pól
- `GET /visits?size=3&sort=startsAt,desc` → 200, trzy najpóźniejsze wizyty
- `./mvnw test` → `BUILD SUCCESS`
- `cd bruno && npx @usebruno/cli@4.1.0 run wizyty-strony --env local` → `Status ✓ PASS`, pięć żądań
