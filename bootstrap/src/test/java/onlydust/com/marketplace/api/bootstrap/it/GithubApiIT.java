package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GithubApiIT extends AbstractMarketplaceApiIT {
    private static final String ONLYDUST_ACCOUNT_JSON = """
            {
              "id": 123456,
              "organization": {
                "id": 98735558,
                "login": "onlydustxyz",
                "avatarUrl": "https://avatars.githubusercontent.com/u/98735558?v=4",
                "htmlUrl": "https://github.com/onlydustxyz",
                "name": "OnlyDust",
                "repos": [
                  {
                    "id": 480776993,
                    "owner": "onlydustxyz",
                    "name": "starklings",
                    "description": "An interactive tutorial to get you up and running with Starknet",
                    "htmlUrl": "https://github.com/onlydustxyz/starklings"
                  },
                  {
                    "id": 481932781,
                    "owner": "onlydustxyz",
                    "name": "starkonquest",
                    "description": "An educational game to learn Cairo where you implement ship AIs that fight to catch as much dust as possible!",
                    "htmlUrl": "https://github.com/onlydustxyz/starkonquest"
                  },
                  {
                    "id": 493591124,
                    "owner": "onlydustxyz",
                    "name": "kaaper",
                    "description": "Documentation generator for Cairo projects.",
                    "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                  },
                  {
                    "id": 498695724,
                    "owner": "onlydustxyz",
                    "name": "marketplace-frontend",
                    "description": "Contributions marketplace backend services",
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                  },
                  {
                    "id": 593218280,
                    "owner": "onlydustxyz",
                    "name": "octocrab",
                    "description": "A modern, extensible GitHub API Client for Rust.",
                    "htmlUrl": "https://github.com/onlydustxyz/octocrab"
                  },
                  {
                    "id": 593701982,
                    "owner": "onlydustxyz",
                    "name": "gateway",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/gateway"
                  }
                ]
              }
            }
            """;

    @Autowired
    GithubAppInstallationRepository githubAppInstallationRepository;

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
                .jsonPath("$.message").isEqualTo("Installation 0 not found");
    }
}
