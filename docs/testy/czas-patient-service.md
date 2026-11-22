# Czas testów: `PatientServiceTest` i `PatientServiceSpringTest`

Pomiar z 22.11.2026 na tej samej maszynie. Każdą klasę puściłem osobno trzy razy
(`./mvnw -q surefire:test -Dtest=<klasa>` po `./mvnw -q test-compile`), a potem cały build
(`./mvnw -q test`). Czasy klas i metod są z raportów surefire w `target/surefire-reports`.

| Klasa | Testy | Osobno: klasa | Osobno: z Mavenem | W pełnym buildzie |
|---|---|---|---|---|
| `PatientServiceTest` (Mockito, bez Springa) | 23 | 1,8–1,9 s | ok. 9 s | 0,17 s |
| `PatientServiceSpringTest` (`@SpringBootTest`, H2) | 1 | 11,7–12,0 s, sam test 0,74 s | ok. 19 s | 0,29 s |

Pełny build: 161 testów w 26,8 s.

## Co z tego wynika

- **Kontekst kosztuje więcej niż test.** Puszczony osobno `PatientServiceSpringTest` trwa prawie 12 s,
  a sama metoda testowa 0,74 s. Reszta to start kontekstu: Liquibase zakłada schemat na H2, Hibernate
  sprawdza encje, wstają Spring Security, MVC i reszta beanów.
- **23 testy jednostkowe są szybsze niż jeden test na kontekście.** Z 1,8 s osobnego przebiegu
  `PatientServiceTest` prawie wszystko zjada pierwszy test (rozgrzanie JVM i Mockito). W pełnym
  buildzie, na rozgrzanej JVM, cała klasa trwa 0,17 s.
- **W pełnym buildzie test na kontekście jest tani.** Spring TestContext trzyma kontekst w pamięci
  i oddaje go kolejnym klasom z tą samą konfiguracją. Start (12,1 s) zapłacił pierwszy
  `@SpringBootTest` w kolejce, `AuthApiIntegrationTest`. `PatientApiIntegrationTest`,
  `MedicalClinicApplicationTests` i `PatientServiceSpringTest` dostały gotowy kontekst, stąd 0,29 s.
  To odpowiedź na pytanie z TEST-01, czemu test puszczony osobno startuje dłużej niż w całym buildzie.
- **Maven dokłada swoje.** Każde osobne `./mvnw` to około 7 s startu, zanim ruszy pierwszy test.

## Który test do czego

Reguły serwisu (zajęty e-mail, numer dowodu, kolejność usuwania pacjenta i konta) sprawdzają testy
jednostkowe: są szybkie i pokazują dokładnie, która reguła pękła. `PatientServiceSpringTest` ma jeden
test na to, czego atrapy nie pokażą: prawdziwe transakcje, schemat z Liquibase, BCrypt i mapowanie
na DTO w transakcji serwisu przy `open-in-view=false` (zapis i odczyt idą w osobnych transakcjach,
więc konto pacjenta musi dać się dociągnąć w trakcie `findById`).
