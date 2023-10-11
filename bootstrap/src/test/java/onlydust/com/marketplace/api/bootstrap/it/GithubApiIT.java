package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GithubApiIT extends AbstractMarketplaceApiIT {
    private static final String ONLYDUST_ACCOUNT_JSON = """
            {
              "organization": {
                "name": "onlydustxyz",
                "logoUrl": "https://avatars.githubusercontent.com/u/98735558?v=4"
              },
              "repos": [{
                "name": "marketplace-frontend",
                "shortDescription": "Contributions marketplace backend services",
                "githubId": 498695724
              }]
            }
            """;

    @Autowired
    GithubAccountRepository githubAccountRepository;

    @Test
    void should_get_github_account_from_installation_id() {
        client.get()
                .uri(getApiURI(GITHUB_INSTALLATIONS_GET + "/123456"))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(ONLYDUST_ACCOUNT_JSON);
    }

    @Test
    void should_return_404_when_not_found() {
        client.get()
                .uri(getApiURI(GITHUB_INSTALLATIONS_GET + "/0"))
                .exchange()
                // Then
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("NOT_FOUND");
    }
}
