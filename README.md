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

### Sign your commits

Commits must be signed with GPG.

To setup GPG:

```bash
brew install gnupg

# Generate a new GPG key (see https://docs.github.com/en/authentication/managing-commit-signature-verification/generating-a-new-gpg-key for full details)
# When asked to enter your email address, ensure that you enter the verified email address for your GitHub account.
gpg --full-generate-key

# List GPG keys
gpg --list-secret-keys --keyid-format=long
# /Users/hubot/.gnupg/secring.gpg
# ------------------------------------
# sec   4096R/3AA5C34371567BD2 2016-03-10 [expires: 2017-03-10]
# uid                          Hubot <hubot@example.com>
# ssb   4096R/4BB6D45482678BE3 2016-03-10
# Copy the GPG key ID (eg. `3AA5C34371567BD2`)

# Prints the GPG key ID, in ASCII armor format (replace `3AA5C34371567BD2` with your GPG key ID)
gpg --armor --export 3AA5C34371567BD2
# Copy the GPG key, starting with `-----BEGIN PGP PUBLIC KEY BLOCK-----` and ending with `-----END PGP PUBLIC KEY BLOCK-----`
```

Then, [add the GPG key to your GitHub account](https://docs.github.com/en/authentication/managing-commit-signature-verification/adding-a-gpg-key-to-your-github-account).

Then, [configure Git to use your GPG key](https://docs.github.com/en/authentication/managing-commit-signature-verification/telling-git-about-your-signing-key):

```bash
git config --global --unset gpg.format
git config --global user.signingkey 3AA5C34371567BD2
git config --global commit.gpgsign true

brew install pinentry-mac
echo "pinentry-program $(which pinentry-mac)" >> ~/.gnupg/gpg-agent.conf
killall gpg-agent
```

Optionally, [configure JetBrains to sign commits](https://www.jetbrains.com/help/idea/2024.2/set-up-GPG-commit-signing.html?Set_up_GPG_commit_signing&utm_source=product&utm_medium=link&utm_campaign=IU&utm_content=2024.2#u7gxqt_31):

> Start IntelliJ IDEA (or restart it to make sure it loads the changes you've made to your environment).
>
> In the Settings dialog, go to Version Control | Git, and click the Configure GPG Key button.
>
> In the dialog that opens, click Sign commits with GPG key and select the key you want to use from the list.
>
> Now every commit will be signed with the selected key. The state of the GPG signature will be displayed in the Commit details pane on the Log tab.

