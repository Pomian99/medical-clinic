### Zadanie 1 (12 min). Wyszukiwanie pacjenta po e-mailu
Dodaj drugi wariant ścieżki `/patients`: żądanie z parametrem `email` ma zwrócić
jednego pacjenta, a żądanie bez parametru ma nadal zwracać całą listę. Nie twórz nowej
ścieżki. W kontrolerze dopisz metodę z warunkiem `params = "email"` i parametrem
oznaczonym `@RequestParam`. W serwisie dopisz metodę, która oddaje `Optional<Patient>`
z repozytorium; repozytorium ma już `findByEmail(String)` z lekcji 7.

Brak pacjenta obsłuż tak samo jak przy szukaniu po `id`: status 404 bez body.

Kryteria akceptacji:
- [ ] `GET /patients` nadal zwraca 200 i całą listę.
- [ ] `GET /patients?email=anna.nowak@example.com` zwraca 200 i jednego pacjenta.
- [ ] `GET /patients?email=nikt@example.com` zwraca 404.
- [ ] W serwisie nie ma liczby 404, a w kontrolerze nie ma pętli po liście pacjentów.

### Zadanie 2 (13 min). Zmiana samego hasła
Dodaj `PATCH /patients/{id}/password`. Body ma jedno pole:

```json
{
  "password": "nowe-haslo-2026"
}
```

Odbierz je jako `Map<String, String>` i weź wartość spod klucza `password`. W serwisie
dopisz metodę, która znajduje pacjenta po `id`, ustawia mu nowe hasło i zwraca go jako
`Optional<Patient>`. Kontroler zamienia pusty wynik na 404, tak jak w zadaniu 1.

Mapa w body zostaje tylko na tę lekcję i tak ją traktuj: nie buduj na niej niczego
więcej. W lekcji 10 dostanie własną klasę z nazwą, która mówi, o co prosi klient.

Kryteria akceptacji:
- [ ] `PATCH /patients/1/password` zwraca 200, a w odpowiedzi jest nowe hasło.
- [ ] `GET /patients/1` pokazuje to samo nowe hasło, a pozostałe pola są bez zmian.
- [ ] `PATCH /patients/99/password` zwraca 404.
- [ ] Ścieżka kończy się rzeczownikiem `password`, a nie czasownikiem.

### Zadanie 3 (5 min). Oba nowe żądania w kolekcji
Załóż w kolekcji Bruno folder `pacjenci` i zapisz w nim dwa dzisiejsze żądania:
wyszukiwanie po e-mailu i zmianę hasła. Resztę CRUD-a dołożysz w pracy domowej.

Kryteria akceptacji:
- [ ] Folder `pacjenci` jest w repozytorium, w katalogu `bruno`.
- [ ] Każde żądanie używa `{{baseUrl}}`, a nie wpisanego na sztywno adresu.
- [ ] Oba żądania wysłane z Bruno zwracają 200 dla pacjenta o `id` równym 1.

## Jak sprawdzisz, że skończyłeś

- `POST /patients` (Anna) → 201, `"id": 1`
- `GET /patients` → 200, tablica z jednym pacjentem
- `GET /patients?email=anna.nowak@example.com` → 200, jeden pacjent
- `GET /patients?email=nikt@example.com` → 404
- `PATCH /patients/1/password` → 200, nowe hasło w odpowiedzi
- `PATCH /patients/99/password` → 404
- `PUT /patients/1` → 200 (tego nie ruszałeś)
- `DELETE /patients/1` → 204

# Lekcja 9. Endpointy w praktyce: praca domowa

## Zadanie D1. Przegląd własnych ścieżek

Przejdź po wszystkich adnotacjach mapujących w swoim projekcie i sprawdź je listą
z zajęć. Ścieżka nazywa zasób rzeczownikiem, operację niesie metoda HTTP,
a identyfikatorem w ścieżce jest `{id}`, nie e-mail. Popraw to, co nie przechodzi.

W terminalu, w katalogu projektu, ta komenda wypisze wszystkie twoje mapowania naraz:

```bash
grep -rhoE "@(Get|Post|Put|Patch|Delete)Mapping(\([^)]*\))?" src/main/java | sort -u
```

Kryteria akceptacji:
- [ ] Żaden adres nie zawiera słowa opisującego operację (`/all`, `/create`, `/update`,
  `/delete`); operację niesie metoda HTTP.
- [ ] Żaden adres nie zawiera e-maila ani innych danych osobowych.
- [ ] Wyszukiwanie po e-mailu jest parametrem zapytania, nie ścieżką.
- [ ] Aplikacja startuje, a wszystkie żądania z lekcji 8 nadal działają.

## Zadanie D2. Kolekcja Bruno z kompletem żądań

Uzupełnij folder `pacjenci` z zajęć tak, żeby zawierał wszystkie żądania projektu:
tworzenie, listę, pacjenta po `id`, pacjenta po e-mailu, pełną aktualizację, zmianę
hasła i usunięcie. Każde żądanie ma używać zmiennej `{{baseUrl}}` ze środowiska `local`.
Kolejność przejścia ustawia pole `seq` w plikach `.bru`: tworzenie pacjenta pierwsze,
usunięcie ostatnie.

Uruchom całą kolekcję od góry na świeżo uruchomionej aplikacji i zapisz, jaki status
wrócił dla każdego żądania. Jeżeli masz zainstalowany Node, cały folder uruchomisz
jedną komendą z katalogu `bruno`:

```bash
npx --yes @usebruno/cli@4.1.0 run pacjenci --env local
```

Kryteria akceptacji:
- [ ] Folder `pacjenci` jest w repozytorium razem z resztą projektu.
- [ ] Kolejność żądań (`seq`) pozwala przejść kolekcję od góry bez ręcznych poprawek.
- [ ] Statusy zgadzają się z tym, czego oczekujesz: 201, 200, 200, 200, 200, 200, 204.

## Zadanie D3. Notatka z krokowania doDispatch

Postaw breakpoint w klasie `DispatcherServlet`, w metodzie `doDispatch` (w IntelliJ
znajdziesz klasę skrótem do wyszukiwania klas). Uruchom aplikację w trybie debug
i wyślij jedno żądanie `GET /patients/1` z Bruno. Przechodź krokami, aż wykonanie
zatrzyma się w twojej metodzie kontrolera.

Zapisz notatkę w pliku `docs/dispatcher/droga-zadania.md`. Ma odpowiadać na trzy pytania:
przez które trzy klasy Springa przeszło żądanie, zanim trafiło do twojej metody; w którym
momencie tekst `"1"` z adresu stał się liczbą; co widać w oknie zmiennych, gdy wykonanie
stoi w `doDispatch`.

Kryteria akceptacji:
- [ ] Notatka wymienia trzy klasy z pełnymi nazwami i metodami, w kolejności od wejścia.
- [ ] Jest w niej zdanie o tym, gdzie dzieje się konwersja argumentu.
- [ ] Plik jest w repozytorium projektu.

## Lektury

Do przeczytania przed następnymi zajęciami (obie pozycje w całości):

- `tomaytotomato.com/overloading-rest-endpoints-on-spring-boot-2`: kilka metod pod jedną
  ścieżką rozróżnianych parametrami, czyli dzisiejsze `params` z innej strony.
- `medium.com`, „Spring Boot: Query Parameter vs Path Variable" (Daryl Goh): utrwalenie
  różnicy między parametrem zapytania a zmienną ścieżkową, tym razem w adnotacjach.

## Jak sprawdzisz, że skończyłeś

- Komenda z zadania D1 wypisuje same adresy bez słów opisujących operację.
- Cała kolekcja `pacjenci` przechodzi od góry na świeżo uruchomionej aplikacji.
- Plik `docs/dispatcher/droga-zadania.md` istnieje i wymienia trzy klasy.
- Umiesz opowiedzieć drogę żądania od gniazda sieciowego do swojej metody bez patrzenia
  w notatkę. Od tego zaczniemy następne zajęcia.
