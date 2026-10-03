# Medical Clinic

## API Documentation

The API description is generated from the code by springdoc and is available at two addresses
while the application is running locally:

- `http://localhost:8080/v3/api-docs` - the OpenAPI description as JSON, for machines
  (Bruno, Postman and client generators can import it),
- `http://localhost:8080/swagger-ui/index.html` - a clickable page for humans.

The documentation is disabled in production. The `prod` profile
(`src/main/resources/application-prod.properties`) sets
`springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`, so both
addresses return 404 while the API itself keeps working. Activate the profile with
`java -jar target/medical-clinic-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`.

**What it does not check:** the OpenAPI description says what the API *declares*, not what
it *does*. Nothing executes it, so an endpoint can declare a `409` and never return it, and
the description will not notice. The Bruno collection in `bruno/` verifies what the API
actually does and should be run after every change. One does not replace the other.
