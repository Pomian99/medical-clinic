# Plan: paginacja `GET /visits` (MED-28 cz. 1, L34 Z1)

## 1. Które endpointy stronicuję, a które zostają listami

| Endpoint | Decyzja | Dlaczego |
|---|---|---|
| `GET /visits` (nowy, wszystkie wizyty) | **strona** | Wizyt przybywa najszybciej: każdy doktor to kilkadziesiąt slotów tygodniowo, a stare wizyty zostają w bazie. Lista wszystkich rośnie bez końca. |
| `GET /visits?doctorId=` | lista | Zadanie każe zostawić ją bez zmian. Wizyt jednego doktora jest dużo mniej, ale też rosną, więc to kandydat na następną część. |
| `GET /visits/patient/{id}` | lista | Wizyt jednego pacjenta jest kilka do kilkudziesięciu, długo zmieszczą się na jednym ekranie. |
| `GET /doctors`, `GET /facilities` | lista | Kilkanaście, kilkadziesiąt pozycji. Przychodnia nie zatrudnia tysięcy lekarzy. |
| `GET /patients`, `GET /users` | lista, na razie | Rosną z każdą rejestracją, więc kiedyś będą wymagały stron. Poza zakresem tego zadania. |

Uwaga do kodu: dziś `GET /visits` z wymaganym `?doctorId` jest zmapowany przez samo `@GetMapping`.
Nowy endpoint bez parametrów też byłby `@GetMapping`, więc Spring zgłosiłby niejednoznaczne
mapowanie. Istniejący endpoint dostanie `@GetMapping(params = "doctorId")`, tak jak
`GET /patients?email=` w `PatientController`.

## 2. Domyślny rozmiar i sortowanie

- Rozmiar: **20**.
- Sortowanie: **`startsAt` rosnąco, potem `id`**, czyli kolejność kalendarza.

Samo `startsAt` nie wystarczy: dwóch doktorów może mieć wizytę o tej samej godzinie. Przy remisie baza
może zwrócić wiersze w innej kolejności przy każdym zapytaniu, a wtedy ta sama wizyta trafi na dwie
strony albo na żadną. `id` jest unikalne, więc rozstrzyga remisy.

## 3. Pola do sortowania

`startsAt`, `endsAt`, `id`.

Pola `doctor` i `patient` oraz ich pola zagnieżdżone (np. `doctor.lastName`) pomijam: sortowanie po nich
wymaga złączenia tabel, a klient i tak może pobrać wizyty konkretnego doktora przez `?doctorId=`.

Dziś Spring Data przyjmie dowolne pole encji, a nieistniejące zakończy się wyjątkiem i odpowiedzią 500.
Ograniczenie do tej listy i odpowiedź 400 to osobny krok, nie w tej części.

## 4. `Page` czy `Slice`

`Page`: recepcja przegląda grafik stronami „1 2 3” i potrzebuje liczby wszystkich wizyt, a dodatkowe
`count` na tabeli wizyt przychodni jest tanie.

## 5. Kształt odpowiedzi

```json
{
  "content": [
    { "id": 6, "doctorId": 1, "patientId": null, "startsAt": "...", "endsAt": "..." }
  ],
  "page": 1,
  "size": 5,
  "totalElements": 12,
  "totalPages": 3
}
```

`content` to lista `VisitDto` (encje nie wychodzą z API). Rekord `PageResponse<T>` w pakiecie `dto`
zamiast surowego `Page`, którego struktura należy do Springa.

## 6. Testy w L35

Test wycinka `VisitControllerTest` (`@WebMvcTest`, dubler serwisu oddaje `PageImpl` z otrzymanego
`Pageable`):

1. Parametry `page`, `size`, `sort` trafiają do serwisu (`ArgumentCaptor` na `Pageable`).
2. Bez parametrów działa domyślna strona: 0, rozmiar 20, sortowanie `startsAt`, `id`.
3. `size=5000` zostaje przycięte do 100.
4. Odpowiedź ma dokładnie pięć pól `PageResponse`, a `content` ma pola `VisitDto`.
5. `GET /visits?doctorId=1` dalej zwraca listę, a nie stronę.

Test jednostkowy `VisitServiceTest`: `findAll(Pageable)` przekazuje ten sam `Pageable` do repozytorium.

## Co się zmieniło (22.11.2026, poprawki po recenzji PR #31)

- Punkty 1, 2, 4 i 5 weszły do kodu tak, jak w planie.
- Punkt 2: `VisitService` dokłada `id` jako ostatni klucz także wtedy, gdy klient poda własne `sort`
  (np. `?sort=startsAt,desc`), bo `@PageableDefault` działa tylko bez tego parametru (4c4542c).
- Punkt 3: ograniczenie do `startsAt`, `endsAt`, `id` weszło później: inne pole daje 400 z nazwą pola
  i listą dozwolonych.
- Punkt 6: `VisitServiceTest` nie sprawdza „tego samego `Pageable`”, tylko dokładanie `id`
  (`findAll_sortWithoutId_addsIdAsLastSortKey`, `findAll_sortWithId_keepsClientSort`). Testów wycinka
  `VisitControllerTest` z tego punktu nie ma; stronicowanie sprawdza folder `visit-pages` w Bruno.
- Swagger: `page`, `size` i `sort` są osobnymi parametrami (`@ParameterObject`), a oba warianty
  `GET /visits` opisuje jedna operacja (23baab0).
