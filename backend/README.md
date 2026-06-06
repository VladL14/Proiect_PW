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

- Auth: `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`.
- Players: `/api/players`, `/api/players/{playerId}`, stats, history, abilities, avatar operations.
- Matches: `/api/matches`, filtered match listing, match state, join/start/status/player removal, JSON replay.
- Rounds: `/api/matches/{matchId}/rounds`, round details, roll, lock, locked-dice, target, resolve.
- Abilities: `/api/abilities`, player abilities, ability activation.
- Emotes (social): `/api/matches/{matchId}/emotes` (send + poll), with membership validation and a 3s cooldown.
- Admin (ADMIN role only): `/api/admin/users`, `/api/admin/matches`, `/api/admin/match-history`, `/api/admin/server-status`.
- Files and ability packs: avatar upload/replacement/removal, replay export (text), ability-pack import and metadata management.
- Real-time game protocol: WebSocket at `ws://localhost:3000/ws/game` (see below).

## Authentication & ACL

Accounts are created through `/api/auth/register` (passwords are hashed with
PBKDF2WithHmacSHA256 — never stored in clear text). Login returns a bearer
token that must be sent as `Authorization: Bearer <token>`. Roles are
`GUEST`, `USER`, `ADMIN`. The backend is the source of truth: ACL-protected
handlers are guarded by `@RequireRole`, so a request is rejected with `401`
(unauthenticated) or `403` (wrong role) regardless of what the frontend shows.

A default administrator is seeded on first start:

```text
username: admin
password: admin123
```

## Separate Real-Time Game Protocol (WebSocket)

A JSON, message-oriented protocol is exposed at `ws://localhost:3000/ws/game`.
Every frame is a JSON object with a `type` discriminator. Inbound:
`CONNECT`, `JOIN_LOBBY`, `START_GAME`, `ACTION`, `STATE_SYNC`, `EMOTE`.
Outbound: `CONNECT_ACK`, `STATE`, `EMOTE`, relays of lobby/action messages, and
`ERROR`. The authoritative game logic stays in the REST/service layer; the
socket provides the real-time fan-out to every client in a match room and is
consumed identically by the web client, the Android client, an A-Frame scene or
a test bot.

## HTTPS

By default the API serves HTTP on port 3000 for local development. To serve
HTTPS (so credentials, tokens, account data and match history are never sent in
clear text), generate a development keystore and run the `secure` profile:

```bash
keytool -genkeypair -alias diceduel -keyalg RSA -keysize 2048 \
        -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
        -validity 365 -storepass changeit -dname "CN=localhost"

mvn spring-boot:run -Dspring-boot.run.profiles=secure
# -> https://localhost:8443/api
```

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
