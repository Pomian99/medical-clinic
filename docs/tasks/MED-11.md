## Zanim zaczniesz
- Stan projektu: z lekcji 12 masz `PatientMapper` z czterema metodami, rekordy wejściowe
  `PatientCreateCommand`, `PatientUpdateCommand` i `EditPasswordCommand` oraz model bez
  przepisywania pól. Jeśli czegoś brakuje, poproś prowadzącego o archiwum stanu `po-L12-pd`.
- Otwórz: IntelliJ z projektem, Bruno z kolekcją medical-clinic, terminal w katalogu projektu.
- Przygotuj sobie dwa body: poprawne (Anna, jak w poprzednich lekcjach) i złe, w którym
  sześć z siedmiu pól łamie inną regułę.

Złe body do wklejenia w Bruno:

```json
{
  "email": "to-nie-jest-email",
  "password": "krotkie",
  "idCardNo": "XYZ",
  "firstName": "",
  "lastName": "Nowak",
  "phoneNumber": "500",
  "birthday": "2044-05-12"
}
```

## Na zajęciach (30 min): MED-11, kryteria 1-3

### Zadanie 1 (8 min). Starter, który sam nic nie robi
Najpierw wyślij złe body na `POST /patients` i zobacz, co dostajesz. Potem dopisz do
`pom.xml` starter walidacji, przebuduj, zrestartuj i wyślij to samo żądanie jeszcze raz.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Wersji nie podajesz: pilnuje jej BOM Boota, tak samo jak przy starterze web z lekcji 6.

Kryteria akceptacji:
- [ ] `grep -n "spring-boot-starter-validation" pom.xml` coś znajduje.
- [ ] Projekt się buduje i aplikacja startuje.
- [ ] Złe żądanie **nadal** zwraca 201 i umiesz powiedzieć, dlaczego.

### Zadanie 2 (14 min). Reguły przy polach
Dopisz adnotacje do komponentów rekordu `PatientCreateCommand`. Każda reguła stoi przy polu,
którego dotyczy, i każda ma własny komunikat.

- `email`: wymagany i w kształcie adresu (`@NotBlank`, `@Email`)
- `password`: wymagane, co najmniej osiem znaków (`@NotBlank`, `@Size`)
- `idCardNo`: wymagany, trzy wielkie litery i sześć cyfr (`@NotBlank`, `@Pattern`)
- `firstName`, `lastName`: wymagane (`@NotBlank`)
- `phoneNumber`: wymagany, dziewięć cyfr (`@NotBlank`, `@Pattern`)
- `birthday`: data z przeszłości (`@Past`)

Komunikat podajesz atrybutem `message`, na przykład
`@NotBlank(message = "first name is required")`. Komunikaty pisz po angielsku, bo
wychodzą do klienta API; tak samo jak komunikaty wyjątków od lekcji 8.

Kryteria akceptacji:
- [ ] Każde z siedmiu pól ma co najmniej jedną adnotację.
- [ ] Na polach tekstowych jest `@NotBlank`, a nie `@NotNull`.
- [ ] Projekt się buduje, a złe żądanie **wciąż** zwraca 201.

### Zadanie 3 (8 min). Bramka
Dopisz `@Valid` przy parametrze `@RequestBody` w metodzie `create` kontrolera (import
`jakarta.validation.Valid`). Zrestartuj i wyślij oba body: złe i poprawne.

Kryteria akceptacji:
- [ ] Złe żądanie zwraca 400.
- [ ] Poprawne żądanie nadal zwraca 201 z pacjentem.
- [ ] W logu aplikacji po złym żądaniu jest `MethodArgumentNotValidException`.
- [ ] Umiesz powiedzieć, czego w body odpowiedzi 400 **nie ma**.

## Jak sprawdzisz, że skończyłeś

- `POST /patients` ze złym body → 400, z poprawnym → 201.
- `GET /patients` → 200 i na liście nie ma ani jednego pacjenta z pustym imieniem.
- W logu widać nazwy pól i komunikaty, które sam napisałeś.
- 

## Zadanie D1. Reguły na pozostałych wejściach

Na zajęciach zabezpieczyłeś rejestrację. Zostały dwie klasy wejściowe: przez nie klient też
może przysłać bzdurę.

**`PatientUpdateCommand`**: te same reguły co przy rejestracji, ale tylko dla pól, które ta
klasa ma. Zwróć uwagę, czego w niej nie ma: numeru dowodu. Tego pola nie ma celowo, bo
wymaganie mówi, że numer dowodu raz podany się nie zmienia. Kształt klasy jest twardszy niż
jakakolwiek reguła walidacyjna, bo czego nie ma w klasie, tego nie da się przysłać.

**`EditPasswordCommand`**: ta sama reguła długości co przy rejestracji, bo to ta sama reguła.

Do obu dopisz `@Valid` przy parametrze w kontrolerze.

Kryteria akceptacji:
- [ ] `PUT /patients/1` z e-mailem bez małpy zwraca 400.
- [ ] `PATCH /patients/1/password` z hasłem `"abc"` zwraca 400.
- [ ] `grep -l "jakarta.validation" src/main/java/com/github/kurs/medicalclinic/dto/*.java`
  wymienia trzy pliki.

## Zadanie D2. Nazwij walidację biznesową

W `PatientService` od lekcji 8 stoi sprawdzenie, czy e-mail nie jest już zajęty. To jest
walidacja, tylko innego rodzaju: żeby ją wykonać, trzeba znać innych pacjentów, a nie tylko
przysłany obiekt. Żadna adnotacja przy polu tego nie wyrazi.

Dopisz nad tym sprawdzeniem komentarz, który nazywa rzecz po imieniu: że to walidacja
biznesowa, że dlatego mieszka w serwisie i że reguły formalne odsiał już `@Valid`.

Kryteria akceptacji:
- [ ] Komentarz odróżnia walidację biznesową od formalnej.
- [ ] Drugi `POST` z tym samym e-mailem nadal zwraca 409, a nie 400. Umiesz powiedzieć,
  czemu to dwa różne statusy.

## Zadanie D3. Dwa żądania negatywne w kolekcji Bruno

Kolekcja `pacjenci` sprawdza na razie tylko ścieżki, na których wszystko idzie dobrze. Dopisz
dwa żądania, które mają się **nie udać**:

- `zle-dane-400`: `POST /patients` z body, w którym sześć z siedmiu pól łamie inną regułę.
- `krotkie-haslo-400`: `PATCH /patients/1/password` z hasłem krótszym niż osiem znaków.

W polu `docs` każdego z nich zapisz, jakiego statusu się spodziewasz i dlaczego.

```bash
cd bruno
npx --yes @usebruno/cli@4.1.0 run pacjenci --env local
```

Kryteria akceptacji:
- [ ] Kolekcja ma dziewięć żądań i wszystkie przechodzą.
- [ ] Dwa ostatnie kończą się statusem 400.
- [ ] Siedem pierwszych ma statusy dokładnie takie jak w lekcji 12.

## Lektury

Do przeczytania przed następnymi zajęciami:

- `baeldung.com/java-validation`: przegląd adnotacji z przykładami, całość.
- `baeldung.com/spring-boot-bean-validation`: `@Valid` w kontrolerze i co się dzieje po
  odrzuceniu żądania; do sekcji o obsłudze błędów włącznie.

## Jak sprawdzisz, że skończyłeś

- Każda klasa wejściowa ma reguły, a każdy parametr z `@RequestBody` ma `@Valid`.
- Kolekcja `pacjenci` przechodzi w całości: dziewięć żądań na dziewięć.
- Umiesz odpowiedzieć na pytanie, czego klient **nie** dowiaduje się z odpowiedzi 400.
