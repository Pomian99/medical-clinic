## MED-15: uwierzytelnianie basic auth

Przed startem: baza od zera. Do L39.3 budujesz z `-DskipTests`: po włączeniu Spring Security testy padają.

**L39.1. Zależności i łańcuch filtrów**
- `spring-boot-starter-security` pod starterem Liquibase'a i `spring-boot-starter-security-test`
  ze `<scope>test</scope>` pod starterem testów web.
- `config/SecurityConfig`, bean `SecurityFilterChain`: bez CSRF, bez sesji (`SessionCreationPolicy.STATELESS`),
  `POST /patients` tylko dla anonima (`anonymous()`), reszta `authenticated()`, `httpBasic(Customizer.withDefaults())`.
- Bean `PasswordEncoder`: `new BCryptPasswordEncoder()`.
- `curl -s -i http://localhost:8080/patients` → 401 z `WWW-Authenticate: Basic realm="Realm"`, bez `Set-Cookie`.

**L39.2. Konta z tabeli**
- `UserRepository`: `Optional<User> findByEmail(String email)`.
- `service/AccountUserDetailsService` (`@Service`, `implements UserDetailsService`): konto po e-mailu
  z nagłówka, `UserDetails` z hasłem i rolą; konta brak → `UsernameNotFoundException`. Nazwę `User`
  zajmuje twoja encja, więc klasę Spring Security piszesz z pełnym pakietem.
- `PatientService.create` zapisuje `passwordEncoder.encode(...)` (`PasswordEncoder` przez konstruktor).
- `grep -c 'generated security password' app.log` → 0.
- Sprawdź: rejestracja Oli (`ola.kowal@example.com`, `tajne123`) bez nagłówka → 201, jej konto na
  `GET /patients?email=ola.kowal@example.com` → 200, w bazie hash `$2a$10$`; złe hasło → 401; konto
  Anny → 401 (w bazie czysty tekst, poprawka w L39.4); rejestracja z nagłówkiem Oli → 403.
- Nowe konto dostaje 401? Rejestracja zapisała czysty tekst. Po poprawce baza od zera i rejestracja jeszcze raz.

**L39.3. Testy z zabezpieczeniami** (`./mvnw test`: 16 porażek i 2 błędy)
- `PatientControllerTest` i `VisitControllerTest`: `SecurityConfig.class` w `@Import` (wycinek
  `@WebMvcTest` sam go nie ładuje), `@WithMockUser` przy testach zalogowanego; testy rejestracji bez adnotacji.
- `PatientApiIntegrationTest`: po rejestracji loguje się nowym kontem przez `Authorization: Basic`
  (e-mail i hasło sklejone dwukropkiem, w Base64), tym samym hasłem, którym zarejestrował.
- `PatientServiceTest`: atrapa `PasswordEncoder`, a w `create_newEmail_savesPatientWithAccount` stub
  `encode`; w koncie ląduje to, co oddał `PasswordEncoder`.
- Żaden test nie wyłącza zabezpieczeń (bez `excludeAutoConfiguration` i bez konfiguracji „przepuść wszystko").

**L39.4. Hasło kodowane wszędzie**
- `PatientService.changePassword` koduje hasło; w `changePassword_patientExists_setsNewPassword` stub `encode` i asercja na hash.
- `UserService.create` (`POST /users`): maper przepisuje hasło z komendy, po mapowaniu podmień je na hash.
- `data.sql`: hashe BCrypt zamiast haseł, hasła pokazowe w komentarzu nad wpisami. Hash zrobisz
  aplikacją: zarejestruj pacjenta z hasłem z `data.sql` i skopiuj `password` jego konta z bazy.
- Na starej bazie `ON CONFLICT DO NOTHING` zostawi czyste hasła: baza od zera.

**L39.5. `X-Request-Id` także przy 401**
- `RequestIdFilter` z L37 stoi za filtrem Spring Security (kolejność -100), więc 401 wychodzi bez
  nagłówka. Przestaw filtr na początek łańcucha.

Gotowe, gdy (aplikacja zatrzymana, baza od zera, build, start):
- `./mvnw test` → `Tests run: 51, Failures: 0, Errors: 0`
- `GET /patients` bez nagłówka → 401 z `WWW-Authenticate: Basic` i `X-Request-Id`
- `GET /patients` kontem Anny → 200
- `PATCH /patients/1/password` z `{"password": "nowe-haslo-2030"}` → 200; potem stare hasło 401, nowe 200
- konto założone przez `POST /users` (np. `lena.lis@example.com`, `tajne456`, rola `DOCTOR`) → 201 i loguje się swoim hasłem (200)
- `SELECT email, left(password, 7) FROM users ORDER BY id` → wszędzie `$2a$10$`
