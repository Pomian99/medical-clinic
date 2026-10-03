# Lekcja 7: Beany i adnotacje. Praca domowa

Termin: przed następnymi zajęciami. Pracujesz na swoim `medical-clinic`, na stanie z zajęć.

## Zadanie D1. Dwa kanały przypomnień i błąd, który musisz zobaczyć

Przychodnia przypomina pacjentom o wizytach. Utwórz pakiet `notification`, a w nim:

- interfejs `ReminderSender` z jedną metodą `void send(String to, String message)`,
- klasę `EmailReminderSender` z `@Component`, która wypisuje na konsolę
  `e-mail do <adres>: <treść>`,
- klasę `SmsReminderSender` z `@Component`, która wypisuje `SMS do <adres>: <treść>`,
- klasę `VisitReminderService` z `@Service` i `@RequiredArgsConstructor`, z polem
  `private final ReminderSender reminderSender` i metodą `remind(String to)`, która woła
  `send` z treścią `przypomnienie o wizycie jutro o 10:00`.

Żeby zobaczyć, który kanał dostał serwis, dopisz tymczasowo w `main` wywołanie:

```java
var context = SpringApplication.run(MedicalClinicApplication.class, args);
context.getBean(VisitReminderService.class).remind("anna.kowalska@example.com");
```

Uruchom aplikację. Nie wstanie. Skopiuj komunikat błędu do pliku `docs/beany/notatka.md`
i dopisz pod nim dwa zdania własnymi słowami: kto potrzebował beana i czemu kontener nie
umiał wybrać.

Kryteria akceptacji:
- [ ] cztery pliki w pakiecie `notification`, projekt się kompiluje
- [ ] uruchomienie kończy się na `APPLICATION FAILED TO START`
- [ ] w notatce jest pełny blok `Description:` z komunikatu
- [ ] w notatce są dwa zdania wyjaśnienia, nie sam wklejony tekst

## Zadanie D2. Domyślny kanał przez `@Primary`

Ustaw e-mail jako domyślny kanał przychodni: dopisz `@Primary` nad `EmailReminderSender`.
Uruchom aplikację ponownie.

Kryteria akceptacji:
- [ ] aplikacja wstaje, w logu jest `Started MedicalClinicApplication`
- [ ] na konsoli pojawia się wiersz zaczynający się od `e-mail do`
- [ ] w notatce jest jedno zdanie o tym, co robi `@Primary`

## Zadanie D3. Wymuszenie kanału przez `@Qualifier`

Przypomnienia o wizycie mają iść SMS-em, choć domyślnym kanałem przychodni zostaje e-mail.
Wymuś to w `VisitReminderService` adnotacją `@Qualifier("smsReminderSender")`.

Zanim napiszesz wersję docelową, przekonaj się na własnej skórze o pułapce z zajęć: postaw `@Qualifier` nad
polem, zostawiając `@RequiredArgsConstructor`, i uruchom. Zapisz w notatce, co się stało.
Potem napisz w tej klasie konstruktor ręcznie i przenieś `@Qualifier` do jego parametru.

Kryteria akceptacji:
- [ ] w notatce jest zdanie o tym, że wersja z adnotacją nad polem wstaje i wysyła e-mail,
  czyli kanał domyślny, a nie ten, o który prosiłeś, i nie zgłasza przy tym błędu
- [ ] `VisitReminderService` ma konstruktor napisany ręcznie, bez `@RequiredArgsConstructor`
- [ ] `@Qualifier("smsReminderSender")` stoi przy parametrze konstruktora
- [ ] po uruchomieniu na konsoli jest wiersz zaczynający się od `SMS do`
- [ ] `EmailReminderSender` nadal ma `@Primary`

## Zadanie D4. Porządki i odpowiedzi

- Skasuj z `main` tymczasowe wywołanie serwisu przypomnień. Klasy z pakietu `notification`
  zostają w projekcie.
- Sprawdź, że `mvn clean test` kończy się na `BUILD SUCCESS` (jeden test `contextLoads` ze
  start.spring.io musi przechodzić: to dowód, że kontekst się podnosi).
- Wypchnij zmiany do swojego repozytorium na GitHubie, tym samym, które założyłeś
  przy MED-01.
- Odpowiedz pisemnie w `docs/beany/notatka.md`, krótko, własnymi słowami:
    1. Czym dla Springa różni się `@Service` od `@Component`, a czym dla czytającego kod?
    2. Czemu `JsonMapper` z biblioteki nie może dostać adnotacji stereotypowej i jak
       wprowadza się go do kontenera?
    3. Klasa ma `@Component`, ale bean nie powstaje i nie ma żadnego błędu. Od czego zaczniesz?

## Jak sprawdzisz, że skończyłeś
- `mvn clean test` → `BUILD SUCCESS`, `Tests run: 1, Failures: 0, Errors: 0`
- aplikacja startuje bez błędu i nic już nie wypisuje z serwisu przypomnień
- w `docs/beany/notatka.md` są: komunikat błędu z D1, zdanie o `@Primary`, zdanie o pułapce
  z `@Qualifier` nad polem i trzy odpowiedzi z D4
- ostatni commit jest na GitHubie

## Lektury
- `baeldung.com/spring-component-repository-service`: czym naprawdę różnią się
  `@Component`, `@Repository` i `@Service`. Całość, tekst krótki.
- `baeldung.com/spring-bean-annotations`: `@Configuration`, `@Bean` i reszta rodziny.
  Czytaj do sekcji o `@Scope`.

## Podpowiedzi (czytaj dopiero, gdy utkniesz)
- D1: aplikacja nie wstanie niezależnie od tego, co robi `main`, bo beany o zakresie singleton
  powstają przy starcie kontenera. Jeśli mimo to wstała, któraś z trzech klas nie jest beanem:
  sprawdź adnotacje `@Component` i `@Service` oraz pakiet, w którym leżą.
- D2: `@Primary` jest w pakiecie `org.springframework.context.annotation`, a nie w tym,
  w którym leżą stereotypy.
- D3: nazwa beana to nazwa klasy małą literą, czyli `smsReminderSender`. Literówka w tej nazwie
  wywoła przy starcie komunikat, że takiego beana nie ma.
- D3: po usunięciu `@RequiredArgsConstructor` z tej jednej klasy import Lomboka przestaje
  być potrzebny; IntelliJ podświetli go jako nieużywany.