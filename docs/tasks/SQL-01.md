### Zadanie 1 (10 min). Baza jedną komendą

Utwórz w katalogu głównym projektu plik `compose.yaml` opisujący jedną usługę: PostgreSQL
w wersji 16. Nazwij usługę `postgres`, a bazę, użytkownika i hasło `clinic`. Wystaw port
5432 (albo 5433, jeżeli 5432 masz zajęty) i dołóż nazwany wolumen, żeby dane przeżywały
zatrzymanie kontenera. Dodaj też `healthcheck`, żeby `docker compose ps` pokazywał
`healthy`. Uruchom bazę w tle i sprawdź, czy stoi.

Kryteria akceptacji:
- [ ] `docker compose up -d --wait` kończy się bez błędu.
- [ ] `docker compose ps` pokazuje usługę `postgres` w stanie `Up` i `healthy`.
- [ ] W `compose.yaml` jest sekcja `volumes` z nazwanym wolumenem.
- [ ] `docker compose down` zatrzymuje bazę, a `docker compose up -d --wait` podnosi ją
  ponownie.

### Zadanie 2 (10 min). Aplikacja łączy się z bazą

Dodaj do `pom.xml` dwie zależności: starter JPA i sterownik PostgreSQL w zakresie
`runtime`. W `src/main/resources/application.properties` dopisz adres JDBC, login i hasło,
zgodne z tym, co wpisałeś w `compose.yaml`. Uruchom aplikację i znajdź w logu dwie rzeczy:
pulę połączeń i adres bazy.

Kryteria akceptacji:
- [ ] `mvn clean package` kończy się na `BUILD SUCCESS`.
- [ ] Aplikacja startuje, a w logu jest wiersz `HikariPool-1 - Start completed`.
- [ ] W logu widać `Database JDBC URL` z adresem bazy i portem, który wybrałeś
  (domyślnie 5432).
- [ ] Po zatrzymaniu kontenera start aplikacji kończy się wyjątkiem z `Connection
      refused`. Tak ma być; podnieś potem bazę z powrotem.

### Zadanie 3 (10 min). Tabela i testy

Napisz `src/main/resources/schema.sql` z tabelą `patients`, odwzorowującą pola klasy
`Patient`. Włącz wykonywanie skryptów, bo PostgreSQL nie jest bazą osadzoną i Spring sam
ich nie uruchomi. Osobno załóż `src/test/resources/application.properties`, żeby testy
chodziły na H2 i nie potrzebowały Dockera; do `pom.xml` dodaj zależność H2 w zakresie
`test`.

Kryteria akceptacji:
- [ ] Po starcie aplikacji `\dt` w bazie pokazuje tabelę `patients`.
- [ ] `\d patients` pokazuje kolumny odpowiadające polom klasy `Patient`.
- [ ] `mvn test` przechodzi przy **zatrzymanym** kontenerze bazy.
- [ ] `GET /patients` zwraca 200 i pustą tablicę: aplikacja nadal czyta listę w pamięci
  i w tej lekcji tak ma być.

## Jak sprawdzisz, że skończyłeś

- `docker compose ps` → usługa `postgres` w stanie `Up` i `healthy`
- start aplikacji → w logu `HikariPool-1 - Start completed` i adres bazy
- `docker compose exec postgres psql -U clinic -d clinic -c "\dt"` → tabela `patients`
- `mvn test` przy zatrzymanym kontenerze → `BUILD SUCCESS`
- `GET /patients` → 200 i pusta tablica, bo lista w pamięci startuje pusta
