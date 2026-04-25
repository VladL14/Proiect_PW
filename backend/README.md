# Dice Duel API

This backend replaces the OpenAPI-generated server stub with a professional Spring Boot application structure.

## Main structure

```text
src/main/java/com/diceduel
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
└── service
    └── impl
```

## Run

```bash
cd backend
mvn spring-boot:run
```

The API runs on:

```text
http://localhost:3000/api
```

Swagger UI:

```text
http://localhost:3000/swagger-ui.html
```

H2 console:

```text
http://localhost:3000/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:diceduel
```

## Implemented layers

- Controllers expose the REST API.
- Services contain business logic.
- Service interfaces include Javadocs.
- Service implementations contain helper methods with Javadocs.
- Repositories use Spring Data JPA.
- Mappers isolate entity-to-DTO conversion.
- Global exception handling returns consistent API errors.
- H2 database is configured for development and testing.
