# Kaskady: co znika po `DELETE /doctors/1`

| Pomiar | `Doctor.user`  | `User.patient` | users przed -> po | patients przed -> po | doctors przed -> po |
|--------|----------------|----------------|:-----------------:|:--------------------:|:-------------------:|
| A      | PERSIST, MERGE | brak           |      4 -> 4       |        4 -> 4        |       1 -> 0        |
| B      | ALL            | ALL            |      4 -> 3       |        4 -> 3        |       1 -> 0        |


## Pomiar A

### Kolejne `delete` z logu
1. `delete from doctor_facility`
2. `delete from doctors`

### Konto i pacjentka po usunięciu
Konto i pacjentka dalej istnieją w systemie

## Pomiar B

### Kolejne `delete` z logu
1. delete from doctor_facility
2. delete from doctors
3. delete from patients
4. delete from users
