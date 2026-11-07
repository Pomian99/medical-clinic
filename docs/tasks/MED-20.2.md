## MED-20: doktorzy i placówki

Przed startem: stan po MED-16 i MED-17. Z MED-20 zostały same wymagania ról.

**L40.5. Reguły** (w `SecurityConfig` pod regułą `POST /visits`, nad regułą dla PATIENT, w tej kolejności)
- `POST /doctors` i `POST /facilities` (nowi doktorzy i placówki) tylko dla ADMIN.
- `GET /doctors` (lista doktorów) tylko dla ADMIN.
- `GET /facilities/**` dla każdego zalogowanego (`authenticated()`).
- `/doctors/*/facilities/*` (przypisanie doktora do placówki i jego cofnięcie) dla DOCTOR.
- Doktor dostaje 403 na przypisaniu? Reguła `/doctors/**` dla PATIENT stoi nad regułą dla DOCTOR.

Gotowe, gdy (baza od zera, build, start):
- `GET /doctors`: Anna 403, admin 200
- `GET /facilities` kontem doktor Katarzyny: 200
- `POST /facilities` jako Anna: 403
- `POST /doctors/1/facilities/1`: Anna 403, doktor Katarzyna 200
- `./mvnw test` → `BUILD SUCCESS`
