## MED-32: JWT zamiast basic auth

Przed startem: stan po MED-18 (albo `po-L40-pd`). Klient podaje hasło raz, pod `POST /auth/token`,
i dostaje token JWT ważny godzinę; dalej wysyła tylko `Authorization: Bearer <token>`. Klinika sama
wystawia token (podpis kluczem prywatnym RSA) i sama go sprawdza (kluczem publicznym), bez Keycloaka.
Bez zmian zostają tabela kont, hasła w BCrypcie, `UserDetailsService` z L39, rejestracja dla anonima
i reguły ról z MED-17, MED-19 i MED-20.

Jak ma działać po zmianie:
- `POST /auth/token` z `{"email": "…", "password": "…"}`: dobre dane → 200 i
  `{"token": "…", "tokenType": "Bearer", "expiresIn": 3600}`; złe hasło albo nieznany e-mail → 401
  z tym samym `wrong e-mail or password`; brak pola → 400 z listą błędów.
- Token RS256, claimy: `iss` = `medical-clinic`, `sub` = e-mail konta, `iat`, `exp` (godzina po `iat`)
  i `roles` z nazwami ról bez przedrostka, np. `["PATIENT"]`.
- Reszta API przyjmuje tylko token: bez tokenu, ze zmienionym tokenem i z nagłówkiem Basic → 401.
- Swagger UI widzi tylko admin, bez okna logowania w przeglądarce. JSON dokumentacji:
  `curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/v3/api-docs`.

**L41.1. Klucze, koder i dekoder**
- `spring-boot-starter-security-oauth2-resource-server` pod starterem Spring Security.
- `JwtConfig`: para kluczy RSA generowana przy starcie (`KeyPairGenerator.getInstance("RSA")`,
  `initialize(2048)`), bean `JwtEncoder` (`NimbusJwtEncoder.withKeyPair(...)`) i bean `JwtDecoder`
  (`NimbusJwtDecoder.withPublicKey(...)` z walidatorem `JwtValidators.createDefaultWithIssuer(...)`).
- Wystawca w publicznej stałej `JwtConfig.ISSUER`; korzystają z niej dekoder i serwis tokenów.
- Klucze żyją w pamięci, więc po restarcie stare tokeny przestają działać. Na produkcji para kluczy przychodzi z pliku albo z sejfu.

**L41.2. Logowanie pod `POST /auth/token`**
- Rekordy `LoginCommand` (`email`, `password`, oba wymagane) i `TokenDto` (`token`, `tokenType`, `expiresIn`).
- `TokenService`: e-mail i hasło sprawdza `AuthenticationManager`
  (`authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password))`), czas
  wystawienia z beana `Clock` z L25, claimy przez `JwtClaimsSet.builder()`. Do `roles` bierzesz tylko
  uprawnienia z `ROLE_` i obcinasz przedrostek.
- Bean `AuthenticationManager` (`ProviderManager` z `DaoAuthenticationProvider` i `PasswordEncoder`)
  w osobnej klasie konfiguracji, nie w `SecurityConfig`: wycinki `@WebMvcTest` nie mają
  `UserDetailsService` i padają z `No qualifying bean`.
- `AuthController` z `POST /auth/token` i adnotacjami springdoc jak w MED-23 (200, 400, 401).
- Advice: handler `AuthenticationException` → 401 z `wrong e-mail or password`, nad piętrem czwartym,
  bez e-maila w logu (MED-30). Bez niego złe hasło kończy się 500.
- `SecurityConfig`: `POST /auth/token` z `permitAll()` zaraz pod regułą rejestracji. Basic auth zostaje do L41.3.
- `JwtPeek.java` z materiału L41 do `scripts/`; w komentarzu skreśl słowo „Keycloaka".
- Sprawdź: token Anny (polecenie niżej) → `java scripts/JwtPeek.java "$TOKEN"` pokazuje `"alg":"RS256"`,
  `"iss":"medical-clinic"`, `"sub":"anna.nowak@example.com"`, `"roles":["PATIENT"]`, `exp` większe od
  `iat` o 3600. Basic auth działa jak dotąd (`GET /patients/1` Anny → 200), a z tokenem to samo żądanie
  dostaje jeszcze 401.

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"email": "anna.nowak@example.com", "password": "silne123"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

`ADMIN_TOKEN` zapisujesz tak samo, z danymi `admin`/`admin`.

**L41.3. Token zamiast basic auth**
- `SecurityConfig`: bez `httpBasic`, z `oauth2ResourceServer` i JWT (dekoder z `JwtConfig`); reguła logowania zostaje.
- `@Import(JwtConfig.class)` nad `SecurityConfig`: wycinki `@WebMvcTest` importują `SecurityConfig`,
  ale innych klas konfiguracji nie ładują, a łańcuch z resource serverem bez dekodera nie wstaje.
- `application.properties`: role w claimie `roles`, przedrostek `ROLE_` (dwa wpisy). `issuer-uri` nie
  jest potrzebne, dekoder budujesz sam.

**L41.4. Testy**
- `PatientApiIntegrationTest` loguje się jak klient: pod `POST /auth/token`, potem wysyła token
  (`JsonPath.read(body, "$.token")` z odpowiedzi logowania).
- `AuthApiIntegrationTest` (pełny kontekst, MockMvc), cztery testy: admin dostaje token na godzinę,
  złe hasło 401, basic auth na `GET /patients` 401, `Bearer abc` 401.
- Dwa wpisy mapowania ról powtórz w `src/test/resources/application.properties`; bez nich token
  w teście ma tylko uprawnienie `FACTOR_BEARER` i dostaje 403.

**L41.5. Bruno i Postman na tokenach**
- Folder `logowanie`, cztery żądania bez uwierzytelnienia (`auth { mode: none }` w `folder.bru`,
  `auth: none` w każdym żądaniu). Trzy logują Annę, admina i doktor Katarzynę i zapisują token
  w zmiennej (`bru.setVar("token", res.getBody().token)`, dalej `adminToken`, `doktorToken`); czwarte
  wysyła złe hasło i oczekuje 401.
- `collection.bru` loguje się tokenem Anny (`mode: bearer`, blok `auth:bearer` z `{{token}}`).
  Foldery i żądania, które logowały się adminem albo doktorem, używają `{{adminToken}}` albo
  `{{doktorToken}}`. W żądaniach zmień i tryb w bloku metody (`auth: bearer`), i blok
  (`auth:bearer`): sam blok bez trybu to dalej 401.
- `logowanie/folder.bru`: w bloku `meta` `seq: 1` zamiast numeru nadanego przez Bruno (nie dopisuj
  drugiej linii). Konwerter układa foldery alfabetycznie i bez tego `logowanie` wypadnie po `doktorzy`
  i `konta`, które bez tokenu admina dostaną 401.
- Eksport do `src/main/resources` ponownie, jak w MED-18 (L40.8); potem zatrzymaj aplikację, zbuduj
  i uruchom ją, bo aplikacja czyta plik z jara.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `POST /auth/token`: dobre dane 200 z tokenem, złe hasło i nieznany e-mail 401 z `"detail":"wrong e-mail or password"`, brak pola 400
- z tokenem Anny `GET /patients/1` → 200, `GET /patients` → 403; z tokenem admina `GET /patients` → 200
- bez tokenu i z `-u anna.nowak@example.com:silne123`: `GET /patients/1` → 401 z `WWW-Authenticate: Bearer`
- po restarcie aplikacji token sprzed restartu → 401 z `Invalid signature`, świeżo pobrany działa
- `./mvnw test` → `Tests run: 57, Failures: 0, Errors: 0`, bez działającej aplikacji i bez Dockera
- na świeżej bazie `npx --yes @usebruno/cli@4.1.0 run logowanie --env local` (w katalogu `bruno`) → `4 (4 Passed)`
- na świeżej bazie `npx --yes @usebruno/cli@4.1.0 run logowanie doktorzy konta pacjenci placowki sloty wizyty wizyty-strony zastepstwa --env local` → `52 (52 Passed)`, `68/68` asercji
- w pliku z `/postman/patients` `grep -c '"type": "basic"'` → 0; Newman na świeżej bazie (polecenie
  niżej, `ADMIN_TOKEN` pobrany po ostatnim restarcie) → `requests` 52, `test-scripts` 12,
  `assertions` 3, w kolumnie `failed` same zera

```bash
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/postman/patients > /tmp/kolekcja.json
npx --yes newman@6.2.2 run /tmp/kolekcja.json --folder logowanie --folder doktorzy --folder konta \
  --folder pacjenci --folder placowki --folder sloty --folder wizyty --folder wizyty-strony \
  --folder zastepstwa --env-var baseUrl=http://localhost:8080 --env-var email=anna.nowak@example.com \
  --env-var haslo=silne123 --env-var adminEmail=admin --env-var adminHaslo=admin \
  --env-var doktorEmail=katarzyna.wrobel@example.com --env-var doktorHaslo=silne654
```

Newman odpowiada `collection could not be loaded`? Token był sprzed restartu i plik wyszedł pusty.
