# Dice Duel API

Spring Boot backend for a multiplayer dice game. The API is organized around clear REST resources, service-layer game logic, Spring Data JPA repositories, and an H2 database for local demo/testing.

## Main Structure

```text
src/main/java/com/diceduel
|-- config
|-- controller
|-- dto
|-- entity
|-- exception
|-- mapper
|-- repository
`-- service
    `-- impl
```

## REST Resource Categories

- Players: `/api/players`, `/api/players/{playerId}`, stats, abilities, avatar operations.
- Matches: `/api/matches`, filtered match listing, match state, join/start/status/player removal.
- Rounds: `/api/matches/{matchId}/rounds`, round details, roll, lock, locked-dice, resolve.
- Abilities: `/api/abilities`, player abilities, ability activation.
- Files and ability packs: avatar upload/replacement/removal, replay export, ability-pack import and metadata management.

The OpenAPI specification is available in both:

```text
../api.yaml
src/main/resources/openapi.yaml
```

## Database

The app uses Spring Data JPA with H2:

```properties
spring.datasource.url=jdbc:h2:mem:diceduel
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

Repositories persist players, matches, rounds, abilities, and ability-pack metadata. API tests verify that POST/PUT/PATCH/DELETE changes can be read back through GET endpoints.

## Run The Backend

```bash
cd backend
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:3000/api
```

Swagger UI:

```text
http://localhost:3000/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:3000/v3/api-docs
```

H2 console:

```text
http://localhost:3000/h2-console
```

H2 connection values:

```text
JDBC URL: jdbc:h2:mem:diceduel
User: sa
Password:
```

## Run Tests

```bash
cd backend
mvn test
```

The API scenario tests are in:

```text
src/test/java/com/diceduel/ApiFlowIntegrationTests.java
```

They cover every controller resource group and endpoint family: players, matches, rounds, abilities, nested player abilities, ability activation, avatar upload/replacement/deletion, replay export, and ability-pack import/metadata management.

They also demonstrate automatic property transfer between steps: create resources, save returned IDs, read them back, update them with PUT/PATCH, verify through GET, delete them, and verify 404 responses.

The same API behavior is also documented and executed as a Karate/Gherkin scenario suite:

```text
src/test/resources/com/diceduel/diceduel_api_tests_final.feature
src/test/java/com/diceduel/DiceDuelKarateTests.java
```

## Presentation Demo Checklist

1. Start the backend with `mvn spring-boot:run`.
2. Open Swagger UI at `http://localhost:3000/swagger-ui.html`.
3. Open H2 console at `http://localhost:3000/h2-console` and connect to `jdbc:h2:mem:diceduel`.
4. Run `mvn test` to show automated API scenarios.
5. Show the main resource groups in Swagger: players, matches, rounds, abilities, files/ability packs.
6. Demonstrate persistence: create/update/delete through API calls, then verify through GET endpoints and H2 tables.
7. Show structured error responses for 400, 404, and 409 cases.
