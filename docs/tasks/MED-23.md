## Zanim zaczniesz
- Stan projektu: z lekcji 14 masz `GlobalExceptionHandler` z czterema piętrami i handlerem
  na zły typ w ścieżce, a z lekcji 13 reguły walidacji na trzech klasach wejściowych. Jeśli
  czegoś brakuje, poproś prowadzącego o archiwum stanu `po-L14-pd`.
- Otwórz: IntelliJ z projektem, terminal w katalogu projektu i **przeglądarkę**. Połowa tej
  lekcji dzieje się na stronie, nie w terminalu.
- Wersji springdoc nie zgaduj: podajesz ją jawnie, bo springdoc nie jest starterem Boota
  i BOM jej nie pilnuje, tak samo jak przy MapStructcie w lekcji 12. W tym kursie to `3.1.1`.

## Na zajęciach (30 min): MED-23, kryteria 1-3

### Zadanie 1 (8 min). Jedna zależność, dwa adresy
Dopisz do `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.1</version>
</dependency>
```

Przebuduj, zrestartuj i wejdź w przeglądarce na `http://localhost:8080/swagger-ui/index.html`.
Rozwiń `POST /patients` i zobacz, skąd strona wie, jakie pola przyjmuje to żądanie.

Kryteria akceptacji:
- [ ] `curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/v3/api-docs` zwraca `200`.
- [ ] Strona pod `/swagger-ui/index.html` otwiera się i wymienia wszystkie trzy ścieżki.
- [ ] Umiesz powiedzieć, skąd wzięła się lista ścieżek, skoro nikt jej nie pisał.

### Zadanie 2 (14 min). Nazwij grupę i każdą operację
Dopisz `@Tag` nad klasą `PatientController` (nazwa grupy i jednozdaniowy opis) oraz
`@Operation(summary = ...)` nad **każdą** metodą kontrolera.

Opisy piszesz po angielsku. To nie jest komentarz w kodzie: ten tekst wychodzi do klienta
API tak samo jak komunikaty błędów z lekcji 14.

Kryteria akceptacji:
- [ ] `curl -s http://localhost:8080/v3/api-docs | python3 -m json.tool | grep -c summary`
  zwraca liczbę **mniejszą** niż liczba metod kontrolera. Umiesz powiedzieć dlaczego.
- [ ] Na stronie Swagger UI każda operacja ma nazwę zrozumiałą bez zaglądania w kod.
- [ ] Grupa endpointów ma nazwę z `@Tag`, nie `patient-controller`.

### Zadanie 3 (8 min). Odpowiedzi, o których opis jeszcze nie wie
`POST /patients` potrafi odpowiedzieć 201, 400 i 409, ale opis wymienia tylko jedną z tych
możliwości. Dopisz `@ApiResponses` z trzema `@ApiResponse`: kod i krótki opis po angielsku.

Kryteria akceptacji:
- [ ] W opisie `POST /patients` są trzy kody: 201, 400 i 409.
- [ ] Każdy ma opis, z którego wynika, **kiedy** klient go dostanie.
- [ ] Reszta endpointów nadal ma po jednym kodzie: to jest praca domowa.

## Jak sprawdzisz, że skończyłeś

- Dwa adresy odpowiadają: `/v3/api-docs` i `/swagger-ui/index.html`.
- Każda operacja w opisie ma własną nazwę, a grupa ma nazwę z `@Tag`.
- `POST /patients` wymienia trzy kody odpowiedzi.
- Umiesz odpowiedzieć na pytanie, czego ten opis **nie** wie o twoim API.

## Zadanie D1. Odpowiedzi negatywne dla wszystkich endpointów

Na zajęciach opisałeś odpowiedzi tylko dla `POST /patients`. Reszta endpointów nadal
deklaruje jeden kod, choć każdy z nich potrafi odpowiedzieć co najmniej dwoma kodami.

Przejdź po kolei przez metody kontrolera i dla każdej dopisz `@ApiResponse` dla **każdego** statusu, który ta
metoda naprawdę zwraca. Podpowiedź, gdzie szukać prawdy: `GlobalExceptionHandler`
z lekcji 14 i reguły walidacji z lekcji 13.

Do **każdej** odpowiedzi negatywnej dopisz też `@Content` ze schematem `ProblemDetail`.
Bez tego opis twierdzi, że przy 404 wraca `PatientDto`, bo springdoc bierze schemat z typu
zwracanego przez metodę. Importy: `io.swagger.v3.oas.annotations.media.Content`,
`io.swagger.v3.oas.annotations.media.Schema`, `org.springframework.http.ProblemDetail`.

Kryteria akceptacji:
- [ ] `GET /patients/{id}` i `GET /patients?email=` wymieniają 200 i 404.
- [ ] `PUT /patients/{id}` wymienia 200, 400 i 404.
- [ ] `PATCH /patients/{id}/password` wymienia 200, 400 i 404.
- [ ] `DELETE /patients/{id}` wymienia 204 i 404.
- [ ] Żaden opis nie wymienia kodu, którego endpoint nie zwraca. Wypisz je z kodu,
  nie z pamięci.
- [ ] Każda odpowiedź 4xx wskazuje schemat `ProblemDetail`, a odpowiedzi pozytywne nadal
  wskazują `PatientDto`. Sprawdzisz to tak:
  `curl -s http://localhost:8080/v3/api-docs | python3 -m json.tool | grep -c ProblemDetail`
  ma zwrócić liczbę większą od zera.

## Zadanie D2. Profil, w którym dokumentacji nie ma

Utwórz `src/main/resources/application-prod.properties` i wyłącz w nim oba adresy
dokumentacji. Dopisz na górze komentarz mówiący, **dlaczego** to robimy.

Potem zbuduj jar i uruchom go dwa razy: raz normalnie, raz z `--spring.profiles.active=prod`.
Za każdym razem sprawdź trzy adresy: `/v3/api-docs`, `/swagger-ui/index.html` i `/patients`.

Kryteria akceptacji:
- [ ] Z profilem `prod` oba adresy dokumentacji zwracają 404, a `/patients` nadal 200.
- [ ] Bez profilu wszystkie trzy zwracają 200.
- [ ] To ten sam jar w obu przebiegach. Umiesz powiedzieć, co dokładnie się zmieniło.

## Zadanie D3. Jedno zdanie o granicy

W pliku `README.md` projektu (jeśli go nie ma, utwórz) dopisz krótką sekcję „Dokumentacja API" i napisz w niej trzy rzeczy:
pod jakim adresem stoi, że na produkcji jest wyłączona i **czego nie sprawdza**.

To ostatnie zdanie jest najważniejsze: opis OpenAPI mówi, co API deklaruje, a kolekcja Bruno
z lekcji 12 sprawdza, co API robi. Jedno nie zastępuje drugiego i za pół roku nikt już tego nie
będzie pamiętał, więc niech będzie zapisane.

Kryteria akceptacji:
- [ ] Sekcja podaje oba adresy i sposób wyłączenia na produkcji.
- [ ] Jest w niej zdanie, które odróżnia opis od kolekcji Bruno.

## Lektury

Do przeczytania przed następnymi zajęciami:

- `springdoc.org` (strona główna i sekcja „Getting Started"): co wciąga ta zależność i jakie
  właściwości `springdoc.*` są dostępne.
- `swagger.io/specification`: sam standard OpenAPI 3.1, sekcje „Paths Object"
  i „Responses Object". Czytaj pobieżnie. Chodzi o to, żeby zobaczyć, że `summary`
  i `description`, które wpisujesz w adnotacji, to pola z tego standardu.

## Jak sprawdzisz, że skończyłeś

- Każdy endpoint wymienia wszystkie statusy, które naprawdę zwraca, i żadnego więcej.
- Ten sam jar z profilem `prod` nie oddaje dokumentacji, a API działa.
- W `README.md` jest napisane, gdzie stoi dokumentacja i czego nie sprawdza.
