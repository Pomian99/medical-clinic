# Klasyfikacja 12 testów (MED-09, L29 Z1)

## Pojęcia

Rodzaje testów:
- **Jednostkowy**: jedna klasa w izolacji, bez Springa, bazy i sieci. Zależności zastąpione
  dublerami albo ich nie ma. Trwa milisekundy.
- **Integracyjny**: kilka elementów razem albo kod z prawdziwą infrastrukturą (kontekst Springa,
  baza, warstwa HTTP). Sprawdza, czy części ze sobą współpracują.
- **E2E**: cały system uruchomiony jak na produkcji, test przechodzi przez niego z perspektywy
  użytkownika albo klienta API.
- **Manualny**: wykonuje go człowiek, nie kod.

Dublery:
- **Stub**: zwraca przygotowane odpowiedzi. Test sprawdza wynik działania kodu, nie to, jak kod
  korzystał ze stuba.
- **Mock**: test sprawdza interakcję, czyli czy i z jakimi argumentami metoda została wywołana
  (`verify`).
- **Fake**: działająca, uproszczona implementacja (np. repozytorium na `HashMap`), która nie nadaje
  się na produkcję.

## Odpowiedzi

| #  | Opis (skrót)                                                       | Rodzaj      | Dubler | Uzasadnienie                                                                                                                       |
|----|--------------------------------------------------------------------|-------------|--------|------------------------------------------------------------------------------------------------------------------------------------|
| 1  | `PatientService` przez `new`, repozytorium zwraca „Jan Kowalski"   | jednostkowy | stub   | Serwis tworzony ręcznie bez Springa, atrapa tylko podaje dane, a test sprawdza wynik (nazwisko).                                   |
| 2  | Cały kontekst Springa z H2, aplikacja wstaje                       | integracyjny| brak   | Startuje naraz wszystkie beany, JPA i bazę, więc sprawdza konfigurację i współpracę wielu elementów, a nie jedną klasę.             |
| 3  | Kasowanie pacjenta, czy serwis zawołał `deleteById(5)`             | jednostkowy | mock   | Metoda nic nie zwraca, więc jedyne, co da się sprawdzić, to wywołanie na repozytorium (`verify`).                                   |
| 4  | Playwright: rejestracja, logowanie i wizyta w przeglądarce         | E2E         | brak   | Przechodzi przez cały system, od interfejsu użytkownika do bazy, tak jak prawdziwy użytkownik.                                      |
| 5  | `VisitRepository` na prawdziwej bazie w kontenerze                 | integracyjny| brak   | Sprawdza zapytanie repozytorium na prawdziwym PostgreSQL, a poprawność SQL da się potwierdzić tylko z bazą.                         |
| 6  | `VisitService` z zegarem zatrzymanym na 7.01.2030 10:00            | jednostkowy | stub   | `Clock.fixed` zawsze zwraca ten sam czas, więc „teraz" jest przygotowaną odpowiedzią, a test daje ten sam wynik każdego dnia.       |
| 7  | Testerka przez godzinę wysyła do API dziwne dane                   | manualny    | brak   | Wykonuje go człowiek (testowanie eksploracyjne), nie ma kodu testu.                                                                |
| 8  | `PatientService` z repozytorium napisanym na `HashMap`             | jednostkowy | fake   | Ręcznie napisane repozytorium naprawdę zapisuje i odczytuje dane, tylko w pamięci zamiast w bazie.                                  |
| 9  | `POST /patients` bez serwera, serwis jest atrapą: 400 i `errors`   | integracyjny| mock   | Startuje wycinek Springa (MVC, JSON, walidacja, advice) i sprawdza ich współpracę. Serwis nie powinien zostać wywołany: `verify(never())`. |
| 10 | Folder `zastepstwa` w Bruno na działającej aplikacji z bazą        | E2E         | brak   | Cały system jest uruchomiony, a test wchodzi przez publiczne API jak klient (E2E na poziomie API, bez interfejsu użytkownika).       |
| 11 | Serwis zastępstw przy nieistniejącym doktorze nie woła `findAllByDoctorId` | jednostkowy | mock | Test sprawdza brak interakcji (`verify(..., never())`), a nie zwracany wynik.                                                  |
| 12 | `Visit.isFree()` na obiekcie z buildera                            | jednostkowy | brak   | Obiekt domenowy bez zależności, więc nie ma czego zastępować.                                                                      |

Bez dublera (6): **2, 4, 5, 7, 10, 12**.

## Czemu opis 2 nie jest testem jednostkowym

- Nie testuje jednej klasy, tylko to, czy cała aplikacja (beany, konfiguracja, JPA, H2) daje się
  złożyć i uruchomić.
- Może paść z powodów niezwiązanych z żadną konkretną klasą: brak beana, zła właściwość
  w `application.properties`, błąd w mapowaniu encji.
- Trwa sekundy, a nie milisekundy, bo startuje kontekst Springa.

## Przypadki na granicy

- **2 i H2.** Baza w pamięci bywa podawana jako przykład fake'a. Tutaj jednak test niczego nie
  podmienia w kodzie: H2 to zwykła konfiguracja bazy w `src/test/resources`, a aplikacja korzysta
  z niej przez prawdziwe JPA. Dlatego „brak".
- **9 i `@WebMvcTest`.** Bywa nazywany testem jednostkowym kontrolera, ale startuje część kontekstu
  Springa i sprawdza kontroler razem z serializacją JSON, walidacją i obsługą wyjątków. Dlatego
  integracyjny (test wycinka).
- **10 i Bruno.** Bywa nazywany testem API albo testem systemowym. Z czterech rodzajów z zadania
  najbliżej mu do E2E, bo wymaga uruchomionej całej aplikacji z prawdziwą bazą.
