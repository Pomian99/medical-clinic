# Rollback zastępstwa (MED-26, D2)

## Jak mierzone

- Kod z commita `cc1fc06` (MED-26 cz. 1 po D1, D3 i D4, jeszcze bez `@Version` z MED-26.2).
- Czysta baza PostgreSQL 16 przed każdym wariantem, schemat z `ddl-auto=update`, dane z `data.sql`.
- `logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG`, SQL z `spring.jpa.show-sql=true`
  (wiersze `Hibernate: ...`).
- Scenariusz z folderu Bruno `substitutions`: konto i doktor zastępca (id 2), dwa sloty doktora 1
  (11.02.2030 o 10:00 i 11:00), własny slot zastępcy o 11:00, potem `POST /doctors/1/substitutions`
  z `{"substituteId": 2}`.

Stan przed zastępstwem, ten sam w obu wariantach:

```text
 id | doctor_id |      starts_at
----+-----------+---------------------
  1 |         1 | 2030-02-11 10:00:00
  2 |         1 | 2030-02-11 11:00:00
  3 |         2 | 2030-02-11 11:00:00
```

Wizyta 1 (10:00) nie koliduje z niczym u zastępcy, więc pętla ją przenosi. Wizyta 2 (11:00) trafia na
wizytę 3 zastępcy i pętla rzuca `VisitOverlapsException`. Oba warianty odpowiadają tak samo:

```text
409 {"detail":"Doctor with id 2 already has a visit overlapping 2030-02-11T11:00 - 2030-02-11T11:30", ...}
```

Wiersze logu poniżej są bez znacznika czasu, poziomu, PID i wątku.

## Wariant A: bez transakcji, `save` w pętli

```java
    // @Transactional zdjęte na czas pomiaru
    public List<Visit> substitute(long doctorId, long substituteId) {
        ...
        for (Visit visit : visits) {
            if (visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(substituteId, visit.getEndsAt(), visit.getStartsAt())) {
                throw new VisitOverlapsException(substituteId, visit.getStartsAt(), visit.getEndsAt());
            }
            visit.setDoctor(substitute);
            visitRepository.save(visit);
        }
        return visits;
    }
```

`SELECT id, doctor_id, starts_at FROM visits ORDER BY id;` po zastępstwie:

```text
 id | doctor_id |      starts_at
----+-----------+---------------------
  1 |         2 | 2030-02-11 10:00:00
  2 |         1 | 2030-02-11 11:00:00
  3 |         2 | 2030-02-11 11:00:00
```

Wizyta 1 jest u zastępcy, wizyta 2 została u doktora 1. `GET /visits?doctorId=1` zwraca już tylko
wizytę 2.

Przebieg w logu (bez wierszy `Found thread-bound EntityManager`, `Exposing JPA transaction`
i `Not closing pre-bound JPA EntityManager`):

```text
o.s.orm.jpa.JpaTransactionManager : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.existsById]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
Hibernate: select count(*) from doctors d1_0 where d1_0.id=?
o.s.orm.jpa.JpaTransactionManager : Initiating transaction commit
o.s.orm.jpa.JpaTransactionManager : Committing JPA transaction on EntityManager [SessionImpl(1496784161<open>)]
o.s.orm.jpa.JpaTransactionManager : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
Hibernate: select d1_0.id,d1_0.first_name,d1_0.last_name,d1_0.specialization,d1_0.user_id from doctors d1_0 where d1_0.id=?
o.s.orm.jpa.JpaTransactionManager : Initiating transaction commit
o.s.orm.jpa.JpaTransactionManager : Committing JPA transaction on EntityManager [SessionImpl(1496784161<open>)]
Hibernate: select v1_0.id,v1_0.doctor_id,v1_0.ends_at,v1_0.patient_id,v1_0.starts_at from visits v1_0 where v1_0.doctor_id=? order by v1_0.starts_at
Hibernate: select v1_0.id from visits v1_0 where v1_0.doctor_id=? and v1_0.starts_at<? and v1_0.ends_at>? fetch first ? rows only
o.s.orm.jpa.JpaTransactionManager : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
o.s.orm.jpa.JpaTransactionManager : Initiating transaction commit
o.s.orm.jpa.JpaTransactionManager : Committing JPA transaction on EntityManager [SessionImpl(1496784161<open>)]
Hibernate: update visits set doctor_id=?,ends_at=?,patient_id=?,starts_at=? where id=?
Hibernate: select v1_0.id from visits v1_0 where v1_0.doctor_id=? and v1_0.starts_at<? and v1_0.ends_at>? fetch first ? rows only
i.g.p.m.e.GlobalExceptionHandler : Domain error: Doctor with id 2 already has a visit overlapping 2030-02-11T11:00 - 2030-02-11T11:30
```

## Wariant B: stan z D1

`substitute` ma `@Transactional` i nie woła `save`.

`SELECT id, doctor_id, starts_at FROM visits ORDER BY id;` po zastępstwie:

```text
 id | doctor_id |      starts_at
----+-----------+---------------------
  1 |         1 | 2030-02-11 10:00:00
  2 |         1 | 2030-02-11 11:00:00
  3 |         2 | 2030-02-11 11:00:00
```

Tabela jest taka sama jak przed zastępstwem.

Cztery wiersze logu:

```text
o.s.orm.jpa.JpaTransactionManager : Creating new transaction with name [io.github.pomian99.medical_clinic.service.VisitService.substitute]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Hibernate: update visits set doctor_id=?,ends_at=?,patient_id=?,starts_at=? where id=?
o.s.orm.jpa.JpaTransactionManager : Initiating transaction rollback
o.s.orm.jpa.JpaTransactionManager : Rolling back JPA transaction on EntityManager [SessionImpl(1715866565<open>)]
```

Cały przebieg (bez wierszy `Found thread-bound EntityManager`, `Exposing JPA transaction`
i `Not closing pre-bound JPA EntityManager`):

```text
o.s.orm.jpa.JpaTransactionManager : Creating new transaction with name [io.github.pomian99.medical_clinic.service.VisitService.substitute]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
o.s.orm.jpa.JpaTransactionManager : Participating in existing transaction
Hibernate: select count(*) from doctors d1_0 where d1_0.id=?
o.s.orm.jpa.JpaTransactionManager : Participating in existing transaction
Hibernate: select d1_0.id,d1_0.first_name,d1_0.last_name,d1_0.specialization,d1_0.user_id from doctors d1_0 where d1_0.id=?
Hibernate: select v1_0.id,v1_0.doctor_id,v1_0.ends_at,v1_0.patient_id,v1_0.starts_at from visits v1_0 where v1_0.doctor_id=? order by v1_0.starts_at
Hibernate: select v1_0.id from visits v1_0 where v1_0.doctor_id=? and v1_0.starts_at<? and v1_0.ends_at>? fetch first ? rows only
Hibernate: update visits set doctor_id=?,ends_at=?,patient_id=?,starts_at=? where id=?
Hibernate: select v1_0.id from visits v1_0 where v1_0.doctor_id=? and v1_0.starts_at<? and v1_0.ends_at>? fetch first ? rows only
o.s.orm.jpa.JpaTransactionManager : Initiating transaction rollback
o.s.orm.jpa.JpaTransactionManager : Rolling back JPA transaction on EntityManager [SessionImpl(1715866565<open>)]
i.g.p.m.e.GlobalExceptionHandler : Domain error: Doctor with id 2 already has a visit overlapping 2030-02-11T11:00 - 2030-02-11T11:30
```

## Czemu w A zostaje stan w połowie

Bez `@Transactional` na metodzie zastępstwo nie jest jedną operacją. Metody `existsById`, `findById`
i `save` pochodzą z `SimpleJpaRepository`, więc każda otwiera i zatwierdza własną, krótką transakcję.
W logu widać to jako trzy pary `Creating new transaction` i `Committing`. Metody z nazwy
(`findAllBy...`, `existsBy...`) nie mają nawet własnej transakcji.

`save` wizyty 1 kończy się commitem. Przy zatwierdzaniu Hibernate wysyła `update visits`, a PostgreSQL
zapisuje zmianę na stałe. Dopiero potem pętla sprawdza wizytę 2 i rzuca wyjątek. Wyjątek przerywa
pętlę, ale nie ma już czego cofnąć: zmiana wizyty 1 jest zatwierdzona, a wizyta 2 nie zdążyła się
zmienić. Klient dostaje 409, choć połowa zastępstwa jest już w bazie.

## Czemu w B nie

`@Transactional` otwiera jedną transakcję na całą metodę (`Creating new transaction with name
[...VisitService.substitute]`), a wywołania repozytorium do niej dołączają (`Participating in existing
transaction`). Commit może nastąpić dopiero po wyjściu z metody. Metoda kończy się wyjątkiem
(`VisitOverlapsException` jest wyjątkiem niekontrolowanym), więc proxy transakcyjne zamiast commita
robi rollback (`Initiating transaction rollback`, `Rolling back JPA transaction`). PostgreSQL odrzuca
wtedy wszystko, co ta transakcja zrobiła, łącznie ze zmianą wizyty 1.

## Czemu `update` w logu nie oznacza zmiany w bazie

W wariancie B nikt nie woła `save`, a mimo to w logu jest `update visits`. Wizyta 1 jest encją
zarządzaną, więc `setDoctor` tylko ją zmienia w pamięci. Przed następnym zapytaniem o tabelę `visits`
(sprawdzenie kolizji dla wizyty 2) Hibernate robi flush, żeby zapytanie widziało aktualny stan. To ten
flush wysyła `update`.

Wiersz `Hibernate: update ...` mówi tylko, że polecenie poszło do bazy połączeniem tej transakcji.
PostgreSQL wykonuje je wewnątrz otwartej transakcji: zmianę widzi tylko ta transakcja, inne połączenia
dalej widzą starą wartość, a trwała staje się dopiero przy commicie. O losie zmiany decyduje dopiero
wiersz po nim: `Committing` albo `Rolling back`. W B przyszedł rollback, więc `update` przepadł,
co potwierdza `SELECT`. W A ten sam `update` wyszedł w trakcie commita transakcji `save` i dlatego
został w bazie.

## Po pomiarze

Wariant A powstał tylko na czas pomiaru, w osobnej kopii kodu. W repozytorium `substitute` ma
`@Transactional` i nie woła `save`, czyli jest w wariancie B (stan z D1).

## Co się zmieniło od MED-26 (22.11.2026)

Ten sam scenariusz na dzisiejszym kodzie daje to samo: 409, a `SELECT` pokazuje obie wizyty doktora 1
przy nim. Inaczej wygląda log i wywołanie:

- Wizyty mają `@Version` (022924f, MED-26.2), więc `update` sprawdza i podbija wersję:
  `update visits set doctor_id=?,ends_at=?,patient_id=?,starts_at=?,version=? where id=? and version=?`.
  Po rollbacku `version` wizyty 1 w bazie dalej wynosi 0.
- Zastępstwo przenosi tylko wizyty od teraz (00b2f4c), więc pobranie wizyt ma `starts_at>=?`.
- `open-in-view` jest wyłączone (d5854ab). Transakcja sama otwiera EntityManager
  (`Opened new EntityManager`) i sama go zamyka (`Closing JPA EntityManager after transaction`).
  W MED-26 dostawała gotowy z żądania (`Found thread-bound EntityManager` na początku,
  `Not closing pre-bound JPA EntityManager` na końcu).
- `show-sql` usunięty (3eadf1f): SQL idzie przez logger `org.hibernate.SQL` tylko w profilu `dev`
  i ma `[requestId]`, np. `org.hibernate.SQL : update visits ...`.
- Endpoint wymaga tokenu JWT. `POST /doctors/{id}/substitutions` łapie się na regułę `/doctors/**`
  z rolą `PATIENT`, więc z tokenem admina dostaje 403.
