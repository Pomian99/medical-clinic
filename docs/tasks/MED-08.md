# Lekcja 14. Globalna obsługa wyjątków: ćwiczenia

## Zanim zaczniesz
- Stan projektu: z lekcji 13 masz reguły walidacji na trzech klasach wejściowych, `@Valid`
  przy każdym ciele żądania i `PatientAlreadyExistsException` z `@ResponseStatus` jeszcze
  z lekcji 8. Jeśli czegoś brakuje, poproś prowadzącego o archiwum stanu `po-L13-pd`.
- Otwórz: IntelliJ z projektem, Bruno z kolekcją medical-clinic, terminal i okno logu.
- Wyślij `GET /patients/999` i zapamiętaj status, zanim ruszysz kod. Wrócisz do tego
  w Zadaniu 3.

## Na zajęciach (30 min): MED-08, kryteria 1-3

### Zadanie 1 (12 min). Rodzina wyjątków
Utwórz w pakiecie `exception` klasę abstrakcyjną `MedicalClinicException`, która dziedziczy
po `RuntimeException` i trzyma pole `HttpStatus status` z getterem. Konstruktor przyjmuje
komunikat i status.

Przerób `PatientAlreadyExistsException` tak, żeby dziedziczyła po `MedicalClinicException` i podawała
`HttpStatus.CONFLICT` w konstruktorze. Usuń z tej klasy `@ResponseStatus`: status niesie
teraz sam wyjątek.

Dopisz `PatientNotFoundException` z dwoma konstruktorami: jeden przyjmuje `Long id`, drugi `String email`. Oba podają
status `HttpStatus.NOT_FOUND`.

Kryteria akceptacji:
- [ ] `MedicalClinicException` jest abstrakcyjna i ma getter statusu.
- [ ] W katalogu `exception` nie ma już ani jednej adnotacji `@ResponseStatus`:
  `grep -rE "^[[:space:]]*@ResponseStatus" src/main/java/com/github/kurs/medicalclinic/exception/ | wc -l`
  zwraca `0`. Wzorzec jest zakotwiczony na początku wiersza, więc nie liczy wzmianek
  w komentarzach.
- [ ] Projekt się kompiluje.

### Zadanie 2 (10 min). Serwis rzuca, kontroler chudnie
Przestaw `PatientService` tak, żeby zamiast pustego `Optional` rzucał
`PatientNotFoundException`. Dotyczy to `findById`, `findByEmail`, `update`, `changePassword`
i `deleteById`.

Potem uprość kontroler: skoro serwis rzuca, metody nie mają już czego sprawdzać. Zwracają
`PatientDto` zamiast `ResponseEntity<PatientDto>`, a `delete` dostaje
`@ResponseStatus(HttpStatus.NO_CONTENT)` i typ `void`.

> Serwis i kontroler zmieniaj **razem**, jednym podejściem. Między jedną a drugą zmianą
> projekt się nie kompiluje i to jest normalne.

Kryteria akceptacji:
- [ ] `grep -c "notFound" PatientController.java` zwraca `0`.
- [ ] Żadna metoda serwisu nie zwraca już `Optional<Patient>`.
- [ ] Projekt się kompiluje i aplikacja startuje.

### Zadanie 3 (8 min). Advice
Utwórz w pakiecie `exception` klasę `GlobalExceptionHandler` z adnotacjami
`@RestControllerAdvice` i `@Slf4j`. Dodaj jedną metodę obsługującą `MedicalClinicException`:
zaloguj komunikat na poziomie `warn` i zwróć
`ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage())`.

Zrestartuj i sprawdź dwa żądania: drugi `POST` z tym samym e-mailem oraz `GET /patients/999`.

Kryteria akceptacji:
- [ ] Drugi `POST` zwraca 409, `GET /patients/999` zwraca 404.
- [ ] Oba mają `Content-Type: application/problem+json`.
- [ ] W logu są dwa wiersze `WARN`, żadnego `ERROR`.
- [ ] Umiesz powiedzieć, czemu jeden handler wystarczył na dwa różne statusy.

## Jak sprawdzisz, że skończyłeś

- `POST /patients` → 201, drugi raz → 409, `GET /patients/999` → 404, `GET /patients/1` → 200.
- Odpowiedzi z błędem mają pola `detail`, `instance`, `status` i `title`.
- W `PatientController` nie ma ani jednego `ResponseEntity`.

## Zadanie D1. Pozostałe piętra advice

Na zajęciach obsłużyłeś własne wyjątki. Zostały te, które rzuca sam Spring, a odpowiedzi
na nie nadal mają inny kształt niż reszta.

**Walidacja.** Handler dla `MethodArgumentNotValidException` ma zwrócić 400, a do odpowiedzi
dołożyć listę złamanych reguł: dla każdego pola `field` i `message`. Listę dokładasz przez
`problem.setProperty("errors", errors)`. To domyka lukę z lekcji 13, w której klient dostawał
400 bez informacji, co poprawić.

**Nieczytelne body i brak trasy.** Handlery dla `HttpMessageNotReadableException` (400)
i `NoResourceFoundException` (404), oba z krótkim komunikatem po angielsku.

**Siatka bezpieczeństwa.** Handler dla `Exception` zwraca 500 z komunikatem `Unknown error`
i **nic więcej**. Do logu idzie pełny ślad stosu na poziomie `error`; do klienta nie idzie
nic poza tym komunikatem.

Kryteria akceptacji:
- [ ] Odpowiedź 400 z walidacji ma pole `errors` z listą pól i komunikatów.
- [ ] `GET /pacjenci` (zła ścieżka) zwraca 404 z `Content-Type: application/problem+json`.
- [ ] Żaden handler nie wysyła klientowi `exception.getMessage()` na piętrze `Exception`.
- [ ] W logu błędy klienta są na `warn` bez śladu stosu, a wyjątki z piętra czwartego
  na `error` ze śladem.

## Zadanie D2. Błąd klienta, który wygląda jak awaria

Wyślij `GET /patients/abc`. W ścieżce miała być liczba, więc to pomyłka klienta, a odpowiedź
mówi 500, bo żądanie wpadło na siatkę bezpieczeństwa.

Dopisz handler dla `MethodArgumentTypeMismatchException`, który zwraca 400 z komunikatem
mówiącym, które pole ścieżki ma zły typ.

Kryteria akceptacji:
- [ ] `GET /patients/abc` zwraca 400, nie 500.
- [ ] W logu ten przypadek jest na poziomie `warn`, nie `error`.
- [ ] Umiesz wyjaśnić, czemu bez tego handlera żądanie trafiało na piętro czwarte.

## Zadanie D3. Scenariusze błędów w kolekcji Bruno

Dopisz do kolekcji `pacjenci` dwa żądania i uzupełnij `docs` każdego z nich:

- `nieistniejacy-pacjent-404`: `GET /patients/9999`, oczekiwane 404.
- `zly-typ-w-sciezce-400`: `GET /patients/abc`, oczekiwane 400.

```bash
cd bruno
npx --yes @usebruno/cli@4.1.0 run pacjenci --env local
```

Kryteria akceptacji:
- [ ] Kolekcja ma jedenaście żądań i wszystkie przechodzą.
- [ ] Statusy dziewięciu żądań z lekcji 13 nie zmieniają się; zmienia się tylko kształt
  ciała odpowiedzi tam, gdzie żądanie kończy się błędem.

## Lektury

Do przeczytania przed następnymi zajęciami:

- `baeldung.com/exception-handling-for-rest-with-spring`: `@RestControllerAdvice` od podstaw,
  do sekcji o `ProblemDetail` włącznie.
  Uwaga: artykuł dużo miejsca poświęca dziedziczeniu po `ResponseEntityExceptionHandler`.
  Nasz advice tego nie robi i nie musi; czytaj tę część jako wariant, nie jako wymóg.
- `datatracker.ietf.org/doc/html/rfc9457`: sam standard, sekcje 1 i 3. Krótki. Zobaczysz
  w nim, skąd biorą się nazwy pól.

## Jak sprawdzisz, że skończyłeś

- Każda odpowiedź z błędem z twojego API ma `Content-Type: application/problem+json`.
- Wszystkie jedenaście żądań kolekcji `pacjenci` przechodzi.
- Umiesz powiedzieć, co dokładnie zobaczy klient, gdy w serwisie poleci wyjątek, którego
  nikt nie przewidział.
