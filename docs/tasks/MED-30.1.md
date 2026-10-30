## MED-30 cz. 1: logi w serwisach

Przed startem: baza od zera. Aplikację uruchamiasz dopiero w L37.3.
Zasady dla każdego wpisu: placeholdery `{}` (bez sklejania plusem), same identyfikatory (żaden
obiekt ani Command w `{}`), komunikaty po polsku bez diakrytyków (`zarezerwowano wizyte …`).

**L37.1. `VisitService`** (`@Slf4j`)
- INFO: założenie wizyty (wizyta, doktor, początek, koniec), rezerwacja (wizyta, pacjent),
  zastępstwo (liczba wizyt, obaj doktorzy).
- DEBUG: sprawdzanie nakładania przed zapisem, wynik liczenia wolnych slotów.
- Odmów (409, 400) serwis nie loguje, robi to advice.

**L37.2. `PatientService`** (`@Slf4j`)
- INFO: rejestracja (numer zapisanego pacjenta, bez `command`), zmiana danych, zmiana hasła
  (bez hasła), usunięcie.
- `./mvnw test` zielony. Test tworzenia pada z `"saved" is null`? Dopisz
  `when(patientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));`
  (to samo dla `visitRepository` w teście tworzenia wizyty).

**L37.3. Próg i sprawdzenie**
- `logging.level.com.github.kurs.medicalclinic=DEBUG` w `application.properties`, build,
  aplikacja z `> app.log 2>&1`.
- Rejestracja pacjenta, dwa sloty doktora 1 (4.03.2030, 10:00 i 11:00), rezerwacja pierwszego
  dla pacjenta 2, druga próba dla pacjenta 3.
- `grep -E 'service\.(Visit|Patient)Service +:' app.log`: rejestracja, dwa założenia wizyty
  (każde po DEBUG o nakładaniu), rezerwacja, DEBUG z wolnymi slotami.
- Druga rezerwacja: jeden WARN z advice (`blad domenowy: …`), bez stosu.
- W `GlobalExceptionHandler` siatka na `Exception` woła `log.error("…", exception)` (5xx z pełnym stosem).
- Spring i Hibernate nie piszą DEBUG (poza menedżerem transakcji z L26).

Gotowe, gdy:
- `POST /patients` → 201 i wpis `zarejestrowano pacjenta …`
- dwa sloty doktora 1 → 201, 201 i dwa wpisy `zalozono wizyte …`
- `PATCH /visits/1/book/2` → 200 i wpisy `zarezerwowano …` oraz `wolne sloty …`
- `PATCH /visits/1/book/3` → 409 i jeden WARN z advice
- `./mvnw test` → `BUILD SUCCESS`
