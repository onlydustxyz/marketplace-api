# Marketplace API

## Development

### Credentials

In order to be able to download dependencies from the GitHub Maven repository, you need to provide your GitHub username and a personal access token.
Create a file named `settings.xml` in the `~/.m2` directory with the following content:

```xml'
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" 
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

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


