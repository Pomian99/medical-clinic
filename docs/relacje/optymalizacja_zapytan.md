|                | Przed D1 | Po D1 | Po D2 |
|----------------|----------|-------|-------|
| liczba zapytań |     7    |   7   |       |

## Przed D1
```
Hibernate: select d1_0.id,d1_0.first_name,d1_0.last_name,d1_0.specialization,d1_0.user_id from doctors d1_0
Hibernate: select u1_0.id,u1_0.email,u1_0.password,u1_0.role from users u1_0 where u1_0.id=?
Hibernate: select u1_0.id,u1_0.email,u1_0.password,u1_0.role from users u1_0 where u1_0.id=?
Hibernate: select u1_0.id,u1_0.email,u1_0.password,u1_0.role from users u1_0 where u1_0.id=?
Hibernate: select f1_0.doctor_id,f1_1.id,f1_1.city,f1_1.name,f1_1.number,f1_1.street,f1_1.zip_code from doctor_facility f1_0 join facilities f1_1 on f1_1.id=f1_0.facility_id where f1_0.doctor_id=?
Hibernate: select f1_0.doctor_id,f1_1.id,f1_1.city,f1_1.name,f1_1.number,f1_1.street,f1_1.zip_code from doctor_facility f1_0 join facilities f1_1 on f1_1.id=f1_0.facility_id where f1_0.doctor_id=?
Hibernate: select f1_0.doctor_id,f1_1.id,f1_1.city,f1_1.name,f1_1.number,f1_1.street,f1_1.zip_code from doctor_facility f1_0 join facilities f1_1 on f1_1.id=f1_0.facility_id where f1_0.doctor_id=?
```
 - Pierwsze zapytanie pobiera listę doktorów.
 - Trzy kolejne wynikają z domyślnego ustawienia `FetchType.EAGER` na `@OneToOne` dla Doctor.user i są wywoływane automatycznie dla każdego doktora od razu po pierwszym SELECT.
 - Trzy ostatnie są wywołane dla każdego doktora w momencie mapowania Doctor na DoctorDto w mapperze. 

## Po D1
```
Hibernate: select d1_0.id,d1_0.first_name,d1_0.last_name,d1_0.specialization,d1_0.user_id from doctors d1_0
Hibernate: select u1_0.id,u1_0.email,u1_0.password,u1_0.role from users u1_0 where u1_0.id=?
Hibernate: select f1_0.doctor_id,f1_1.id,f1_1.city,f1_1.name,f1_1.number,f1_1.street,f1_1.zip_code from doctor_facility f1_0 join facilities f1_1 on f1_1.id=f1_0.facility_id where f1_0.doctor_id=?
Hibernate: select u1_0.id,u1_0.email,u1_0.password,u1_0.role from users u1_0 where u1_0.id=?
Hibernate: select f1_0.doctor_id,f1_1.id,f1_1.city,f1_1.name,f1_1.number,f1_1.street,f1_1.zip_code from doctor_facility f1_0 join facilities f1_1 on f1_1.id=f1_0.facility_id where f1_0.doctor_id=?
Hibernate: select u1_0.id,u1_0.email,u1_0.password,u1_0.role from users u1_0 where u1_0.id=?
Hibernate: select f1_0.doctor_id,f1_1.id,f1_1.city,f1_1.name,f1_1.number,f1_1.street,f1_1.zip_code from doctor_facility f1_0 join facilities f1_1 on f1_1.id=f1_0.facility_id where f1_0.doctor_id=?
```
 - Pierwsze zapytanie pobiera listę doktorów.
 - Następne dwa zapytania są powtarzane dla każdego doktora w trakcie mapowania Doctor na DoctorDto w mapperze. Są wywoływane, dopiero gdy potrzebujemy dostępu do danych z kolejnych zapytań.

### Zmiany

Liczba zapytań nie zmieniła się, lecz jest inna ich kolejność. Wynika to z `FetchType.Lazy`, które pobiera dane, dopiero kiedy są potrzebne.
