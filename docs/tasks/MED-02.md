## MED-02, model pacjenta i pierwsze beany

### Zadanie 1 (10 min). Model pacjenta
Utwórz pakiet `model`, a w nim klasę `Patient` z polami: `id` typu `Long`, `email`,
`password`, `idCardNo`, `firstName`, `lastName`, `phoneNumber` jako `String` oraz
`birthday` typu `LocalDate`. Dodaj konstruktor bezargumentowy i konstruktor ze wszystkimi
polami, a gettery i settery wygeneruj w IntelliJ (Alt+Insert, w menu „Generate", pozycja
„Getter and Setter", zaznacz wszystkie pola).

Kryteria akceptacji:
- [ ] klasa `Patient` leży w pakiecie `model` pod pakietem klasy głównej
- [ ] `birthday` jest typu `LocalDate`, a nie `String`
- [ ] klasa nie ma nad sobą żadnej adnotacji
- [ ] `mvn clean package` kończy się na `BUILD SUCCESS`

### Zadanie 2 (10 min). Repozytorium jako bean
Utwórz pakiet `repository`, a w nim klasę `InMemoryPatientRepository` i oznacz ją
adnotacją `@Repository`. Trzymaj pacjentów na liście w pamięci, a kolejne identyfikatory
nadawaj licznikiem `AtomicLong` zaczynającym od jedynki. Napisz metody: `findAll`,
`findById` i `findByEmail` (dwie ostatnie zwracają `Optional<Patient>`), `save` (nadaje `id`,
gdy pacjent go nie ma) oraz `deleteById`, która zwraca `boolean`. Jeśli czas goni, zrób
najpierw `findAll`, `findById` i `save`; `findByEmail` i `deleteById` dokończysz w domu.

Kryteria akceptacji:
- [ ] nad klasą stoi `@Repository`
- [ ] pola listy i licznika są `private final`
- [ ] `findById` i `findByEmail` zwracają `Optional`, nie `null`
- [ ] `findAll` zwraca kopię listy, a nie listę trzymaną w repozytorium
- [ ] projekt się buduje

### Zadanie 3 (10 min). Serwis jako bean i dowód z kontenera
Utwórz pakiet `service` z klasą `PatientService` oraz pakiet `exception` z klasą
`PatientAlreadyExistsException` dziedziczącą po `RuntimeException`. Serwis oznacz
adnotacjami `@Service` i `@RequiredArgsConstructor`, a repozytorium trzymaj w polu
`private final`. Napisz metody `findAll`, `findById` oraz `create`, która przed zapisem
sprawdza po e-mailu, czy taki pacjent już istnieje, a jeśli tak, rzuca wyjątek.

Na koniec sprawdź, które z Twoich klas trafiły do kontenera. Dopisz tymczasowo
w `main` przechwycenie kontekstu i wypisanie posortowanych nazw beanów, uruchom aplikację,
znajdź swoje klasy na liście, a potem skasuj te linie.

```java
var context = SpringApplication.run(MedicalClinicApplication.class, args);
Arrays.stream(context.getBeanDefinitionNames()).sorted()
        .forEach(System.out::println);
```