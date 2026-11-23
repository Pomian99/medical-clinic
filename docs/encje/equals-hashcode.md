# equals i hashCode encji (MED-27, L33 D3)

Stan kodu: commit `63de712` (koniec MED-27, po D2). Wyniki testów przy złych wersjach pochodzą z kopii
tego commita, w której po kolei podmieniałem `equals` i `hashCode` we wszystkich pięciu encjach
i uruchamiałem `PatientTest`, `DoctorTest` i `EntityEqualityTest` (razem 19 testów).

## 1. Jak `HashSet` używa `hashCode` i `equals`

`HashSet` trzyma elementy w `HashMap`, a ta w tablicy wiader. Przy `add`, `contains` i `remove`
najpierw liczy `hashCode` i z niego wybiera wiadro. Dopiero w tym jednym wiadrze porównuje elementy
przez `equals`. Jeśli obiekt leży w innym wiadrze, niż wskazuje jego obecny `hashCode`, zbiór go nie
znajdzie, nawet gdy `equals` dałby `true`.

Cichy warunek: `hashCode` elementu nie może się zmienić, dopóki element jest w zbiorze. Kontrakt
`hashCode` pozwala na zmianę, kiedy zmieniają się pola używane w `equals`. Zbiór nic o tym nie wie
i liczy wiadro tylko raz, przy `add`.

Encje łamią ten warunek, bo ich tożsamość zmienia się w trakcie życia. Przed zapisem `id` jest `null`,
przy zapisie baza nadaje mu wartość, a encja może już wtedy leżeć w zbiorze, np. placówka dodana
do `Doctor.facilities` przed zapisem. Dlatego `equals` i `hashCode` encji muszą dawać ten sam wynik
przed zapisem, po nim i po ponownym odczycie z bazy.

## 2. Trzy złe wersje

| Wersja | Padające testy (z 19) |
|---|---|
| domyślna z `Object` | 6: `equals_sameId_*` (5 encji w `EntityEqualityTest` i `PatientTest`) |
| po wszystkich polach (`@EqualsAndHashCode` Lomboka) | 13: test zbioru (5 + `PatientTest`), `equals_noId_*` (5 + `PatientTest`) i `DoctorTest` |
| hash z id (`Objects.hashCode(id)`, `equals` po id) | 7: test zbioru (5 + `PatientTest`) i `DoctorTest` |

**Domyślna** (`Object.equals` porównuje referencje). Scenariusz porażki: ten sam wiersz jako dwa
obiekty. W jednym kontekście utrwalania Hibernate daje dla wiersza zawsze ten sam obiekt, więc tam
wszystko działa. Wystarczy jednak porównać encję z poprzedniego żądania z encją świeżo odczytaną
albo zbudować w teście dwa obiekty z `id` 42: nie są równe, zbiór trzyma ten sam rekord dwa razy,
a `removeFacility` z placówką z innego odczytu niczego nie usuwa. Test zbioru i `DoctorTest`
przechodzą, bo operują na jednym obiekcie.

**Po wszystkich polach** (`@EqualsAndHashCode` albo `@Data`). Scenariusz porażki: `hashCode` zmienia
się przy każdej zmianie pola, także przy nadaniu `id`. Placówka dodana do zbioru doktora przed
zapisem po zapisie jest w złym wiadrze i `removeFacility` jej nie usuwa (`DoctorTest`). Do tego dwaj
niezapisani pacjenci z tymi samymi danymi są równi (`equals_noIdSameFirstName_patientsAreNotEqual`
pada), więc zbiór zostawi tylko jednego z nich. Pola `LAZY` w `equals` dociągają relacje z bazy albo
kończą się `LazyInitializationException`, a przy relacjach dwukierunkowych porównanie wpada
w nieskończoną rekurencję.

**Hash z id** (`equals` po id, `hashCode` z `Objects.hashCode(id)`). Scenariusz porażki: przed zapisem
`hashCode` to 0 (`Objects.hashCode(null)`), po zapisie hash z `id`. Encja leży w wiadrze dla 0,
a `contains` szuka jej w wiadrze dla 42. Testy równości przechodzą, więc wersja wygląda na poprawną,
dopóki encja nie trafi do zbioru przed zapisem.

## 3. Nasza strategia

Ten sam kod w `Patient`, `User`, `Doctor`, `Facility` i `Visit`
(`src/main/java/io/github/pomian99/medical_clinic/model/`), tu z `Patient`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Patient other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

- Równe są tylko dwa obiekty z tym samym, nadanym `id`. Niezapisana encja jest równa tylko sobie
  (`this == o`).
- `hashCode` jest stały dla klasy, więc nadanie `id` go nie zmienia i encja zostaje w swoim wiadrze.
- `instanceof` zamiast `getClass()`: proxy Hibernate'a jest podklasą encji, więc `getClass()`
  dałby różne klasy dla proxy i encji tego samego wiersza.
- `other.getId()` zamiast `other.id`: pola proxy są puste, a getter `id` proxy oddaje identyfikator
  bez ładowania encji.
- Koszt: wszystkie encje jednej klasy lądują w jednym wiadrze, więc wyszukiwanie w zbiorze sprawdza je
  po kolei. Przy zbiorach takich jak placówki doktora to bez znaczenia, przy tysiącach encji w jednym
  zbiorze byłoby wolne.

Testy strażniki (`src/test/java/io/github/pomian99/medical_clinic/model/`), na `63de712` 19 z 19 zielonych:

- `PatientTest.java`: pacjent w zbiorze przed nadaniem `id` jest w nim po nadaniu (Z1), dwaj pacjenci
  z tym samym `id` są równi i mają ten sam `hashCode` (Z1), dwaj niezapisani z tym samym imieniem nie
  są równi (Z3);
- `EntityEqualityTest.java`: te same trzy przypadki jako testy sparametryzowane dla pięciu encji;
- `DoctorTest.java`: placówka dodana przez `addFacility` przed nadaniem `id` daje się usunąć przez
  `removeFacility` po nadaniu, a zbiór placówek zostaje pusty (D2).

## 4. Klucz biznesowy kontra UUID

**Klucz biznesowy** wygrywa, gdy encja ma pole unikalne, wymagane, znane przy tworzeniu obiektu
i niezmienne, np. ISBN książki. `equals` i `hashCode` z tego pola działają przed zapisem, między
sesjami i rozkładają encje po wielu wiadrach. Przegrywa, gdy taka wartość może się zmienić albo nie
każdy rekord ją ma.

**UUID** nadany przy tworzeniu obiektu (`UUID.randomUUID()` w polu) i zapisany w osobnej kolumnie
wygrywa, gdy naturalnego klucza nie ma. Jest stały od pierwszej chwili, znany przed zapisem i rozkłada
encje po wiadrach. Kosztuje dodatkową kolumnę z indeksem unikalnym. Losowy UUID użyty jako klucz główny
rozrzuca wstawienia po całym indeksie.

**`idCardNo` jako klucz pacjenta: nie.** Kolumna jest unikalna i wymagana, a w MED-27 API nie pozwala
jej zmienić (`PatientUpdateCommand` nie ma tego pola). Ale numer dowodu zmienia się przy każdej
wymianie dowodu: po upływie ważności, utracie albo zmianie danych. Pacjent jest dalej tą samą osobą,
a z kluczem z numeru stałby się innym obiektem. Dziecko często nie ma dowodu, a cudzoziemiec ma
paszport. Do tego to dana wrażliwa, której nie chcemy w logach ani w `toString`. Zostajemy przy `id`.

## 5. `toString` encji przy relacjach `LAZY`

Encje nie mają `toString`, więc dziedziczą ten z `Object`: nazwa klasy i hash, np.
`io.github.pomian99.medical_clinic.model.Patient@1b2c3d`. Wygenerowany `toString`
(`@ToString` albo `@Data`) wypisuje wszystkie pola, także relacje. Pole `LAZY` trzyma proxy, a jego
`toString` wymaga załadowania encji.

Sprawdzone na kopii `63de712` z `@ToString` na `Patient` i `User` (H2, `show-sql=true`):

W transakcji `toString` pacjenta wysyła osobne zapytanie o konto:

```text
CHECK in transaction, user loaded before toString: false
Hibernate: select u1_0.id,u1_0.email,u1_0.password,u1_0.role from users u1_0 where u1_0.id=?
CHECK toString: Patient(id=1, idCardNo=LAZ123456, firstName=Jan, lastName=Lazy, phoneNumber=500600700, birthday=null, user=User(id=1, email=lazy@example.com, password=Secret#123, role=PATIENT))
CHECK in transaction, user loaded after toString: true
```

Po wyjściu z transakcji ten sam `toString` się wywraca:

```text
CHECK toString outside transaction threw: org.hibernate.LazyInitializationException: Could not initialize proxy [io.github.pomian99.medical_clinic.model.User#1] - no session
```

Skutki:

- logowanie listy pacjentów daje jedno dodatkowe zapytanie na pacjenta (N+1);
- log albo komunikat poza transakcją kończy się wyjątkiem zamiast wpisem;
- do logu trafiają numer dowodu i hasło;
- przy relacjach dwukierunkowych `toString` wywołuje się w kółko aż do `StackOverflowError`. U nas
  relacje są jednokierunkowe, więc tego problemu nie ma.

Jeśli encja ma mieć `toString`, to tylko z `id` i prostymi polami, bez relacji i danych wrażliwych.

## Co się zmieniło od MED-27 (22.11.2026)

- `equals` i `hashCode` we wszystkich pięciu encjach są takie jak w punkcie 3.
- `PatientTest` usunięty po recenzji PR #31 (b8890f9). Jego trzy testy powtarzały
  `EntityEqualityTest`, który sprawdza to samo dla wszystkich encji.
- Doszedł `EntityEqualityDatabaseTest.java` (d4ae667), strażnik z prawdziwym zapisem do bazy (H2
  w testach), na `Facility`. Encja dodana do zbioru przed `save` jest w nim po nadaniu `id`. Dwa odczyty tego samego
  wiersza dają dwa różne, ale równe obiekty z tym samym `hashCode`. Niezaładowane proxy
  z `getReferenceById` jest równe załadowanej encji.
- Hasła są dziś hashami BCrypt, a nie jawnym tekstem. Wygenerowany `toString` dalej wypisałby hash
  i numer dowodu, więc wniosek z punktu 5 zostaje.
