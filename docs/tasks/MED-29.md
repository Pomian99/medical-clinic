## MED-29: fasada rezerwacji

Przed startem: baza od zera (`docker compose down -v`, `docker compose up -d --wait`), build
(`./mvnw -q -DskipTests package`) i aplikacja z logiem w pliku:
`java -jar target/medical-clinic-0.0.1-SNAPSHOT.jar > app.log 2>&1`.

**L36.1. Podsumowanie i wolne sloty**
- Rekord `dto/BookingSummaryDto(VisitDto visit, PatientDto patient, DoctorDto doctor, long freeSlotsThatDay)`.
- W `VisitRepository` zapytanie `@Query`: liczba wizyt doktora bez pacjenta w przedziale czasu.
- `VisitService.countFreeSlots(Long doctorId, LocalDate day)` z `@Transactional(readOnly = true)`;
  dzień = od `day.atStartOfDay()` włącznie do `day.plusDays(1).atStartOfDay()` wyłącznie.
- Sprawdź: `./mvnw -q compile`.

**L36.2. `VisitBookingFacade`** (pakiet `facade`, `@Service`)
- `BookingSummaryDto book(Long visitId, Long patientId)`: `VisitService.book`, odczyt pacjenta
  i doktora przez ich serwisy, wolne sloty doktora w dniu wizyty, podsumowanie przez istniejące mapery.
- `@Transactional` na metodzie fasady; wolne sloty liczysz po rezerwacji.
- `PATCH /visits/{id}/book/{patientId}` woła wyłącznie fasadę, pozostałe endpointy wizyt zostają przy serwisie.
- Żaden serwis nie wstrzykuje innego serwisu.

**L36.3. Sprawdź** (przebuduj i zrestartuj aplikację)
- Doktor 1: dwa wolne sloty 4.03.2030, 10:00-10:30 i 11:00-11:30 (`POST /visits`).
- `PATCH /visits/1/book/2` → 200 z `visit`, `patient`, `doctor`, `freeSlotsThatDay` = 1.
- W `app.log` jedna linia `Creating new transaction` z metodą fasady, potem `Initiating transaction commit`.
- Druga rezerwacja tego samego slotu → 409, pacjent, którego nie ma → 404.
- Folder Bruno `sloty` oblewa teraz jedną asercję (`res.body.patientId`); poprawka w L36.6.

**L36.4. `VisitBookingFacadeTest`** (`src/test/java/.../facade`)
- Serwisy jako `@Mock`, mapery prawdziwe (`Mappers.getMapper`), fasada składana w `@BeforeEach`.
- Poprawna rezerwacja: `InOrder` przypina tylko `visitService.book` przed `countFreeSlots`,
  asercje na czterech polach podsumowania. Odczyty pacjenta i doktora w dowolnej kolejności.
- Odmowa: `book` rzuca `VisitAlreadyBookedException`, fasada go przepuszcza, serwisy pacjenta
  i doktora niewywołane (`verifyNoInteractions`).

**L36.5. `VisitControllerTest`** z `@WebMvcTest(VisitController.class)`
- `@MockitoBean` na fasadę i na serwis wizyt (inne endpointy), `@Import(VisitMapperImpl.class)`.
- Rezerwacja: 200 i `$.visit.patientId`, `$.patient.fullName`, `$.doctor.lastName`,
  `$.freeSlotsThatDay`; `verifyNoInteractions` na serwisie wizyt.
- Błędy: 409 z `detail` dla zajętego slotu, 404 dla pacjenta, którego nie ma.

**L36.6. Bruno** `bruno/sloty/zarezerwuj-slot.bru` (reszty folderu nie ruszasz)
- Asercje: 200, `res.body.visit.id: eq {{slotId}}`, pacjent, doktor, `freeSlotsThatDay` jest liczbą.

Gotowe, gdy:
- na czystej bazie dwa sloty doktora 1 → 201, 201
- `PATCH /visits/1/book/2` → 200, podsumowanie z `freeSlotsThatDay`
- `grep -oE 'Creating new transaction[^:]*|Initiating.*' app.log | tail -2` → transakcja fasady i commit
- `PATCH /visits/1/book/3` → 409, `PATCH /visits/2/book/99` → 404
- `./mvnw test` → `BUILD SUCCESS` (w tym `VisitBookingFacadeTest` i `VisitControllerTest`)
- na świeżej bazie `cd bruno && npx @usebruno/cli@4.1.0 run sloty --env local` → `Status ✓ PASS`, cztery żądania
