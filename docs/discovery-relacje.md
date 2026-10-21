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
