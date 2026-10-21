## Na zajęciach (30 min): MED-12, część 1

### Zadanie 1 (12 min). PatientDto na wyjściu
Utwórz pakiet `dto`, a w nim rekord `PatientDto` z polami, które klient ma widzieć:
`id`, `email`, `firstName`, `lastName`, `phoneNumber`, `birthday`. Hasła i numeru dowodu
tam nie ma. Dopisz do rekordu statyczną metodę `from(Patient patient)`, która robi z modelu
obiekt wyjściowy.

Przestaw wszystkie trzy odczyty w kontrolerze na `PatientDto`: listę, pacjenta po `id`
i wyszukiwanie po e-mailu. Serwis zostaje bez zmian: nadal pracuje na modelu.

Kryteria akceptacji:
- [ ] `GET /patients` zwraca 200 i tablicę bez pól `password` i `idCardNo`.
- [ ] `GET /patients/1` i `GET /patients?email=...` zwracają ten sam kształt co lista.
- [ ] W `PatientService` nie pojawia się słowo `PatientDto`.
- [ ] Mapowanie modelu na `PatientDto` jest w jednym miejscu, w metodzie `from`.

### Zadanie 2 (13 min). PatientCreateCommand na wejściu
Dodaj w pakiecie `dto` rekord `PatientCreateCommand` z polami, które klientowi wolno podać
przy rejestracji: `email`, `password`, `idCardNo`, `firstName`, `lastName`, `phoneNumber`,
`birthday`. Pola `id` nie ma i to jest cały sens tej klasy.

Przestaw `POST /patients` na `PatientCreateCommand`. Metoda `create` w serwisie przyjmuje
teraz Command, sprawdza unikalność e-maila jak dotąd i przepisuje pola do nowego pacjenta.

Kryteria akceptacji:
- [ ] `POST /patients` z body Anny zwraca 201 i `PatientDto` bez hasła.
- [ ] Drugi `POST` z tym samym e-mailem nadal zwraca 409.
- [ ] Kontroler nie zwraca już klasy `Patient` w odczytach ani nie przyjmuje jej w `POST`.

### Zadanie 3 (5 min). Sprawdź, co się zmieniło
Wyślij `POST /patients` z body, w którym na początku dopiszesz `"id": 999`, i użyj innego
adresu e-mail niż adres Anny. Zobacz, jakie `id` dostał nowy pacjent.

Kryteria akceptacji:
- [ ] Odpowiedź to 201, a `id` nadał serwer: kolejna liczba, nie 999.
- [ ] Umiesz powiedzieć, dlaczego tak jest, bez zaglądania do kodu serwisu.

## Jak sprawdzisz, że skończyłeś

- `POST /patients` (Anna) → 201, `PatientDto` bez hasła, `"id": 1`
- `POST /patients` z `"id": 999` i innym e-mailem → 201, `"id": 2`
- `GET /patients` → 200, tablica bez `password` i `idCardNo`
- `GET /patients/1` → 200, ten sam kształt
- `GET /patients?email=anna.nowak@example.com` → 200, ten sam kształt
- `PUT /patients/1` → 200 (na razie po staremu, to praca domowa)

# Lekcja 10. Model, DTO i warstwy: praca domowa

## Zadanie D1. PatientUpdateCommand na wejściu PUT

Dodaj rekord `PatientUpdateCommand` z polami `email`, `firstName`, `lastName`,
`phoneNumber`, `birthday`. Nie ma w nim `id` (jest w ścieżce), nie ma hasła (zmienia je
`PATCH`) i nie ma numeru dowodu (raz podany, nie zmienia się). Przestaw na niego
`PUT /patients/{id}`; metoda ma zwracać `PatientDto`.

Kryteria akceptacji:
- [ ] `PUT /patients/1` z body bez hasła zwraca 200 i `PatientDto`.
- [ ] Aktualizacja naprawdę zadziałała: `POST` z poprzednim adresem e-mail przechodzi,
  bo adres się zwolnił. (Tego, że hasło i numer dowodu przetrwały, nie sprawdzisz
  żadnym żądaniem: `PatientDto` ich nie pokazuje. Zajrzyj debugerem albo zaufaj temu,
  że Command ich nie niesie.)
- [ ] `PUT /patients/99` nadal zwraca 404.

## Zadanie D2. EditPasswordCommand zamiast mapy

Zastąp `Map<String, String>` w `PATCH /patients/{id}/password` rekordem
`EditPasswordCommand` z jednym polem `password`. Metoda ma zwracać `PatientDto`.

Kryteria akceptacji:
- [ ] `PATCH /patients/1/password` zwraca 200 i `PatientDto`.
- [ ] W kontrolerze nie ma już importu `java.util.Map`.
- [ ] Umiesz powiedzieć, czego ta zmiana **nie** naprawia (podpowiedź: wyślij
  `{"haslo": "x"}` i zobacz, co się stanie).

## Zadanie D3. Model bogaty

Przenieś przepisywanie pól z serwisu do modelu. W klasie `Patient` dodaj statyczną fabrykę
`create(PatientCreateCommand command)` i metodę `update(PatientUpdateCommand command)`.
Serwis ma po tej zmianie wołać `Patient.create(command)` i `existing.update(command)`,
a regułę o unikalnym e-mailu zostawić u siebie.

Kryteria akceptacji:
- [ ] W metodach `create` i `update` serwisu nie ma ani jednego settera. Jeden setter
  zostaje w `changePassword` i to jest w porządku: zmiana hasła nie ma Commanda
  z kompletem pól, więc nie ma czego przenosić do modelu.
- [ ] Reguła o unikalnym e-mailu nadal jest w serwisie i nadal działa (409).
- [ ] Umiesz uzasadnić, czemu jedna z tych reguł jest w modelu, a druga w serwisie.

## Zadanie D4. Kolekcja Bruno na nowych kształtach

Popraw body w kolekcji tak, żeby pasowały do nowych klas wejściowych: żądanie aktualizacji
nie wysyła już hasła ani numeru dowodu. Uruchom całą kolekcję od góry na świeżo
uruchomionej aplikacji.

```bash
cd bruno
npx --yes @usebruno/cli@4.1.0 run pacjenci --env local
```

Kryteria akceptacji:
- [ ] Wszystkie siedem żądań przechodzi, statusy jak dotąd: 201, 200 pięć razy, 204.
- [ ] Żadne body w kolekcji nie zawiera pola `id`.

## Lektury

Do przeczytania przed następnymi zajęciami:

- `softwareskill.pl/encja-a-pojo-valueobject-dto-i-inne`: rodzaje obiektów po polsku, całość.
- `medium.com`, „Anemic Domain Model vs. Rich Domain Model" (Matthias Schenk): oba style
  na przykładach, całość.

## Jak sprawdzisz, że skończyłeś

- Cała kolekcja `pacjenci` przechodzi od góry na świeżo uruchomionej aplikacji.
- W `PatientService` nie ma settera w `create` ani w `update` (jeden zostaje w `changePassword`)
  i nie ma tam ani jednego `PatientDto`.
- W `PatientController` nie ma klasy `Patient` w żadnej sygnaturze.
- Umiesz powiedzieć, co wchodzi do aplikacji, co z niej wychodzi i co zostaje w środku.
