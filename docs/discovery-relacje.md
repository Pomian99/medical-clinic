# Discovery: audyt relacji dwukierunkowych

## 1. Rekurencja w JSON-ie

Żaden kontroler nie zwraca encji. Wszystkie metody zwracają `*Dto` albo `List<*Dto>`.

Gdyby kontroler zwrócił encję, pętla zamknęłaby się na:
- `Doctor.facilities` -> `Facility.doctors` -> `Doctor.facilities` -> ...
- `Doctor.visits` -> `Visit.doctor` -> `Doctor.visits` -> ...
- `User.patient` -> `Patient.user` -> `User.patient` -> ...

### Sposób sprawdzenia
```bash
grep -rnE 'public (List<)?(Doctor|Facility|Patient|User|Visit)>? ' src/main/java/io/github/pomian99/medical_clinic/controller
grep -rn 'import io.github.pomian99.medical_clinic.model' src/main/java/io/github/pomian99/medical_clinic/dto
grep -rn 'mappedBy' src/main/java
```

### Co zrobiono
Brak potrzebnych działań


## 2. Lombok na encjach z relacjami

Encje mają tylko `@Getter`, `@Setter`, `@NoArgsConstructor` i `@AllArgsConstructor` (`User` także `@Builder`).

### Sposób sprawdzenia
```bash
grep -rnE '^ *@(Data|ToString)' src/main/java
```

### Co zrobiono
Brak potrzebnych działań

## 3. Niespójność stron

| Miejsce                                       | Zmiana                    | Strona odwrotna                    | Było                       |
|-----------------------------------------------|---------------------------|------------------------------------|----------------------------|
| `PatientService.create`                       | `patient.setUser(user)`   | `User.patient`                     | zostawała `null`           |
| `VisitService.create`                         | `visit.setDoctor(doctor)` | `Doctor.visits`                    | nie dostawała nowej wizyty |

### Sposób sprawdzenia
Ręczne sprawdzenie serwisów.

### Co zrobiono
Dodałem metody synchronizujące po stronie właściciela, na wzór `Doctor.addFacility`: pierwsza
linijka ustawia stronę właściciela, druga stronę odwrotną.
- `Patient.assignUser(User user)`: `this.user = user` i `user.setPatient(this)`.
- `Visit.assignDoctor(Doctor doctor)`: `this.doctor = doctor` i `doctor.getVisits().add(this)`.

`PatientService.create` i `VisitService.create` wołają je zamiast setterów.

## 4. Kaskady w obie strony

W projekcie są trzy kaskady, z czego dwie leżą na tej samej relacji, po obu jej stronach:
`Patient.user` (`PERSIST`) i `User.patient` (`ALL`). `Doctor.user` nie ma `REMOVE`, więc
usunięcie doktora zatrzymuje się na nim. Z `ALL` przy `Doctor.user` (próba z zadania 3)
`REMOVE` szło dalej: doktor -> `Doctor.user` -> konto -> `User.patient` -> pacjentka.

`REMOVE` z `User.patient` nie da się dziś wywołać przez API: `UserService.deleteById` odrzuca
konto, na którym jest pacjent, zanim Hibernate dostanie `remove`.

| Pole                           | Strona     | Kaskada                           | Test cyklu życia                     | Oczekiwany wynik                                                                     |
|--------------------------------|------------|-----------------------------------|--------------------------------------|--------------------------------------------------------------------------------------|
| `Patient.user`                 | właściciel | `PERSIST`                         | `POST /patients`                     | 201; `insert into users`, potem `insert into patients`                               |
| `Patient.user`                 | właściciel | `PERSIST` (bez `REMOVE`)          | `DELETE /patients/1`                 | 204; tylko `delete from patients`, konto zostaje                                     |
| `Doctor.user`                  | właściciel | `PERSIST`, `MERGE`                | `POST /users`, potem `POST /doctors` | 201; `insert into doctors`, bez `insert into users`                                  |
| `Doctor.user`                  | właściciel | `PERSIST`, `MERGE` (bez `REMOVE`) | `DELETE /doctors/1`                  | 204; `delete from doctor_facility`, `delete from doctors`; konto i pacjentka zostają |
| `User.patient`                 | odwrotna   | `ALL`                             | `DELETE /users/{id}`                 | 409; nic nie znika                                                                   |

### Sposób sprawdzenia
```bash
grep -rn 'cascade' src/main/java
```

### Co zrobiono
Brak potrzebnych działań

## Co się zmieniło (22.11.2026, po recenzji PR #31)

Sekcje 1–4 opisują kod z chwili D1 (`4d0b84f`). Strony odwrotne `Doctor.visits`, `Facility.doctors`
i `User.patient` wtedy istniały, a serwisy wołały `Patient.assignUser` i `Visit.assignDoctor`.
Polecenia z sekcji 1, 2 i 4 dają na tym commicie to, co opisano (sprawdzone 22.11).

Zmianę przyniosło D3 (`5c8cca6`), które usunęło wszystkie strony odwrotne. Tabela, którą D3 miało
dopisać do sekcji 3, nie trafiła wtedy do notatki. Jest poniżej.

### Strony odwrotne: kto je czytał (D3)

| Pole | Kto je czytał przy D1 | Kto je zapisywał | Decyzja |
|---|---|---|---|
| `Doctor.visits` (`mappedBy = "doctor"`) | nikt: wizyty doktora czyta `VisitRepository.findAllByDoctorId`, a `DoctorMapper` miał `ignore` | druga linijka `Visit.assignDoctor` | usunięte |
| `Facility.doctors` (`mappedBy = "facilities"`) | nikt: `FacilityMapper` miał `ignore`, a placówki doktora idą przez stronę właściciela `Doctor.facilities` | druga linijka `Doctor.addFacility` i `Doctor.removeFacility` | usunięte |
| `User.patient` (`mappedBy = "user"`, `ALL`) | nikt: `UserService.deleteById` pyta `patientRepository.existsByUserId`, a `UserMapper` miał `ignore` | druga linijka `Patient.assignUser` | usunięte |

Sposób sprawdzenia, na `4d0b84f`:

```bash
grep -rnE '\.(getVisits|getDoctors|getPatient)\(\)' src/main/java src/test/java
```

Wynik to tylko trzy zapisy: `facility.getDoctors().add(this)`, `facility.getDoctors().remove(this)`
i `doctor.getVisits().add(this)`. Żaden kod, łącznie z testami, nie czytał tych pól.

Decyzja jest świadoma: relacje są jednokierunkowe, bo żaden endpoint ani zapytanie nie nawiguje od
strony odwrotnej. Strona odwrotna wraca dopiero razem z kodem, który ją czyta. W `5c8cca6` zniknęły
trzy pola, ich `ignore` w maperach i druga linijka metod synchronizujących. `assignUser`
i `assignDoctor` robiły już wtedy to samo co setter, więc usunąłem je w całości, a serwisy wróciły do
`setUser` i `setDoctor`. `Doctor.addFacility` i `removeFacility` zostały, z jedną linijką na stronie
właściciela.

### Sekcje 1, 3 i 4 dziś

- Sekcja 1: żadnej z trzech pętli nie da się już zamknąć, bo nie ma pól, przez które szły.
  `grep -rn 'mappedBy' src/main/java` nic nie znajduje. Kontrolery dalej zwracają tylko DTO, a od
  poprawek po recenzji DTO oddają też serwisy.
- Sekcja 3: `Patient.assignUser` i `Visit.assignDoctor` nie istnieją. `PatientService.create` woła
  `patient.setUser(user)`, a `VisitService.create` woła `visit.setDoctor(doctor)`. Bez strony
  odwrotnej nie ma czego synchronizować.
- Sekcja 4: kaskady są dwie, `Patient.user` (`PERSIST`) i `Doctor.user` (`PERSIST`, `MERGE`). Wiersza
  z `User.patient` już nie ma. `DELETE /users/{id}` na koncie pacjenta albo doktora dalej daje 409,
  bo odmawia `UserService.deleteById`. `DELETE /patients/{id}` usuwa dziś pacjenta, a potem jego
  konto (3e91771). Robi to serwis, a nie kaskada. Pacjent z wizytami dostaje 409.
- Wszystkie żądania z tabeli w sekcji 4 wymagają dziś tokenu JWT, poza `POST /patients`, które jest
  tylko dla niezalogowanych.
