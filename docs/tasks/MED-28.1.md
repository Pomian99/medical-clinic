## MED-28 cz. 1: paginacja GET /visits

Przed startem: baza na czysto (`docker compose down -v`, `docker compose up -d --wait`) i aplikacja.

**L34 Z1. Plan** `docs/paginacja/plan-get-visits.md`, sześć odpowiedzi (pokaż prowadzącemu przed
kodowaniem):
1. Które endpointy stronicujesz, które zostają listami, i dlaczego.
2. Domyślny rozmiar i domyślne sortowanie.
3. Pola, po których klient może sortować.
4. `Page` czy `Slice`, jednym zdaniem.
5. Kształt odpowiedzi (pola widoczne dla klienta).
6. Jakie testy napiszesz w L35.

**L34 Z2. `GET /visits` z `Pageable`**
- `VisitService.findAll(Pageable)` z `@Transactional(readOnly = true)`, woła `visitRepository.findAll(pageable)`.
- Endpoint w `VisitController`: `@PageableDefault` według planu, zwraca `Page<VisitDto>`
  przez `Page.map(visitMapper::toDto)`.
- `GET /visits?doctorId=1` i `GET /visits/patient/{id}` działają jak wcześniej.

**L34 Z3. Sprawdź w logu:** dodaj kilkanaście wizyt doktora 1, wyślij `GET /visits?page=1&size=5`.
- Odpowiedź: 5 wizyt, pola `totalElements`, `totalPages`, `number`.
- W logu dwa zapytania: strona (`offset ... fetch first`) i `count`.
- Ostrzeżenie „Serializing PageImpl instances as-is is not supported": umiesz powiedzieć, skąd jest.

**L34 D1. Limit rozmiaru strony**
- `spring.data.web.pageable.max-page-size=100` w `src/main/resources/application.properties`
  i w `src/test/resources/application.properties`, obok komentarz, po co limit.
- `GET /visits?size=5000` oddaje stronę o rozmiarze 100.

**L34 D2. `PageResponse` zamiast surowego `Page`**
- Rekord `PageResponse<T>` w pakiecie `dto`: pola `content`, `page`, `size`, `totalElements`,
  `totalPages` i statyczne `from(Page<T>)`.
- `GET /visits` oddaje dokładnie te pięć pól, `content` z `VisitDto` (encje nie wychodzą).
- Ostrzeżenie o `PageImpl` znika z logu.

Gotowe, gdy:
- `GET /visits?page=1&size=5` → 200, `page` 1, `size` 5, pięć wizyt w `content`
- `GET /visits?size=2&sort=startsAt,desc` → 200, dwie najpóźniejsze wizyty
- `GET /visits?size=5000` → 200, `size` 100
- `./mvnw test` → `BUILD SUCCESS`
