## MED-18: porządek w Liquibasie i kolekcja zapytań

Przed startem: stan po MED-20, aplikacja zatrzymana.

**L40.6. Identyfikatory od 1**
- Kroki w `001-initial.yaml` dostają identyfikatory `1` do `18` w kolejności z pliku (tak je wykonuje
  Liquibase); kroki `19`-`22` już je mają. Na stanie z tagu `po-L38-pd` lub późniejszym kroki są już
  ponumerowane, więc tylko sprawdzasz.
- Nowy identyfikator to dla Liquibase'a nowy krok, a nie stary pod nową nazwą: na starej bazie start
  pada z `relation "doctors" already exists`, więc baza od zera. Zmieniać identyfikatory wolno ci
  tylko dlatego, że historia nie wyszła poza twój komputer; na wspólnym środowisku wykonanych kroków się nie rusza.

**L40.7. Kolekcja Bruno na trzech kontach** (od MED-15 żądania kolekcji poza rejestracją dostają 401)
- `bruno/collection.bru`: basic auth ze zmiennych `{{email}}` i `{{haslo}}`.
- `bruno/environments/local.bru`: `email` i `haslo` Anny, `adminEmail` i `adminHaslo` admina,
  `doktorEmail` i `doktorHaslo` doktor Katarzyny.
- W żądaniach folderów aplikacji `auth: none` → `auth: inherit`, poza rejestracją (`utworz-pacjenta`, `zle-dane-400`).
- Adminem (`auth: basic` i blok `auth:basic` z `{{adminEmail}}` i `{{adminHaslo}}`): `lista-pacjentow`,
  `pacjent-po-emailu`, `szukaj-po-nazwisku`, `utworz-konto-doktora`, `konto-zastepcy`; siedem żądań
  `POST /visits`: `utworz-slot` w folderach `sloty` i `wizyty`, `nakladajacy-slot-409`,
  `slot-nieistniejacy-doktor-404`, `slot-doktora-1000`, `slot-doktora-1100`, `slot-zastepcy-1100`;
  siedem żądań z MED-20: `utworz-doktora`, `doktor-na-nieistniejace-konto-404`,
  `doktor-na-zajete-konto-409`, `lista-doktorow`, `doktor-zastepca`, `utworz-placowke`, `zajeta-nazwa-409`.
- Doktorem (`{{doktorEmail}}`, `{{doktorHaslo}}`): `przypisz-doktora` i `nieistniejaca-placowka-404`.
- Każdy folder aplikacji ma `folder.bru` z `auth { mode: inherit }`; folder `konta` z basic auth admina.
- `usun-konto-pacjenta-409` dostaje 204, a potem admin wszędzie 401? Na świeżej bazie admin ma `id` 1,
  bo Liquibase rusza przed `data.sql`, więc żądanie usuwało admina. Anna ma konto 2: `/users/1` → `/users/2`.
- Folder `users` to zewnętrzne API z L3 (środowisko `test`), dlatego polecenie wymienia foldery aplikacji po kolei.

**L40.8. Eksport do Postmana**
- W skryptach `res.body` → `res.getBody()`: Bruno rozumie oba, a konwerter poprawnie tłumaczy tylko drugi zapis.
- Z plików z blokiem `params:query` usuń ten blok, parametr zostaw w adresie.
- Bruno → Open Collection z katalogu `bruno` projektu → menu kolekcji → Share → Postman, prosto do
  `src/main/resources`, nazwa `medical-clinic.postman_collection` (Bruno dopisze `.json`). Zmienne
  trafiają do pliku bez wartości.

**L40.9. `GET /postman/patients`**
- `PostmanController` oddaje plik jako `String` czytany przez `InputStream` z `ClassPathResource`.
- `GET /postman/**` tylko dla ADMIN, nad `denyAll`.
- Brak pliku to `FileNotFoundException`. Nie łapiesz go: advice z L14 zamienia nieznany wyjątek na 500.
- Test wycinka kontrolera: admin 200, pacjent 403.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `SELECT id, author, filename FROM databasechangelog WHERE orderexecuted IN (1, 18, 19, 22) ORDER BY orderexecuted` → identyfikatory `1`, `18`, `19`, `22`
- `cd bruno && npx --yes @usebruno/cli@4.1.0 run doktorzy konta pacjenci placowki sloty wizyty wizyty-strony zastepstwa --env local` → `Status ✓ PASS`, `48 (48 Passed)`, `61/61` asercji
- `GET /postman/patients`: admin 200 z `Content-Type: application/json`, Anna 403, anonim 401
- plik przeniesiony poza resources (`mv src/main/resources/medical-clinic.postman_collection.json /tmp/`),
  `./mvnw -q clean package -DskipTests`, restart → admin 500; plik z powrotem, ten sam build, restart → 200
  (bez `clean` stary plik zostaje w `target/classes`)
- `./mvnw test` → `Tests run: 53, Failures: 0, Errors: 0`
- Newman (polecenie niżej): `requests` 48, `test-scripts` 9, `assertions` 3, w kolumnie `failed` same zera.
  Statusów Newman tu nie sprawdza (konwerter nie przenosi asercji Bruno), potwierdza je przebieg Bruno.

```bash
curl -s -u admin:admin http://localhost:8080/postman/patients > /tmp/kolekcja.json
npx --yes newman@6.2.2 run /tmp/kolekcja.json --folder doktorzy --folder konta \
  --folder pacjenci --folder placowki --folder sloty --folder wizyty --folder wizyty-strony \
  --folder zastepstwa --env-var baseUrl=http://localhost:8080 --env-var email=anna.nowak@example.com \
  --env-var haslo=silne123 --env-var adminEmail=admin --env-var adminHaslo=admin \
  --env-var doktorEmail=katarzyna.wrobel@example.com --env-var doktorHaslo=silne654
```
