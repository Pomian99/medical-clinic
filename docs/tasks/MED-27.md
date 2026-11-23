## MED-27: equals i hashCode encji

Strategia: dwa obiekty są równe tylko z tym samym, nadanym id; `hashCode` stały dla klasy.
`equals` przez `instanceof` (nie `getClass()`) i getter (`other.getId()`, nie pole).
Na encjach żadnego `@EqualsAndHashCode` ani `@Data`.

**L33 Z1. Obstaw i sprawdź** (`src/test/java/.../model/PatientTest.java`, na obecnej klasie `Patient`)
- Pacjent dodany do `HashSet` przed `setId(42L)` jest w nim po nadaniu id.
- Dwa obiekty `Patient` z tym samym id są równe.
- Przed uruchomieniem zapisz w komentarzu, który test padnie; potem wyjaśnij, czemu.

**L33 Z2.** `equals` i `hashCode` w `Patient` według strategii. Oba testy z Z1 zielone.

**L33 Z3.** Trzeci test: dwaj niezapisani pacjenci (bez id) z tym samym imieniem nie są równi.
Umiesz powiedzieć, co się stanie z testem z setem po zamianie `hashCode` na `Objects.hashCode(id)`.

**L33 D1. Wszystkie encje:** ta sama strategia w `User`, `Doctor`, `Facility` i `Visit`.
Testy z lekcji 30-32 nadal zielone.

**L33 D2. Test strażnik relacji:** `DoctorTest`
- Placówka dodana przez `addFacility` przed nadaniem id daje się usunąć przez `removeFacility`
  po nadaniu id (bez `getFacilities().add(...)`).
- Na końcu set placówek doktora jest pusty.

**L33 D3. Notatka** `docs/encje/equals-hashcode.md`, pięć punktów:
1. Jak `HashSet` używa `hashCode` i `equals` (wiadra) i jaki cichy warunek kontraktu łamią encje.
2. Trzy złe wersje (domyślna, po wszystkich polach, hash z id), każda ze scenariuszem porażki.
3. Nasza strategia: kod i test strażnik (ścieżki plików w repo).
4. Klucz biznesowy kontra UUID: kiedy co wygrywa; czy `idCardNo` nadaje się na klucz pacjenta.
5. `toString` encji: co zrobi wygenerowany `toString` przy relacjach `LAZY`.

Gotowe, gdy: `PatientTest` (3) i `DoctorTest` (1) zielone, `./mvnw test` → `BUILD SUCCESS`,
notatka w `docs/encje/`.
