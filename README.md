# Marketplace API

## Development

### API contracts

We follow a design-first approach.
OpenAPI 3.0 specification is used to define the API contracts.
DTOs and interfaces are generated from the OpenAPI specification.

### Persistence

We use Hibernate as the ORM to interact with the database.
Read-only entities and write entities are separated to avoid trancient state issues when reading and updating
data in the same transaction. This also allows to write custom optimized queries and to fetch data from Postgres views.

- Entities that are used to persist data in the database MUST be located
  in the `postgres-adapter` module, inside the `..entity.write` package.
- Entities that are only used to read data in the database MUST be located
  in the `postgres-adapter` module, inside the `..entity.read` package, or in the
  `read-api` module.
- Read-only entities MUST NOT include any relationship with write entities.
- A read-only entity that is binded to a real table MUST end with `ViewEntity`.
- A read-only entity that is NOT binded to a real table (ie. it is used as a result DTO for custom queries)
  MUST end with `QueryEntity`.
