package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GithubApiIT extends AbstractMarketplaceApiIT {
    private static final String ONLYDUST_ACCOUNT_JSON = """
            {
              
            }
            """;

    @Autowired
    GithubAccountRepository githubAccountRepository;

    @Test
    void should_get_github_account_from_installation_id() {
        client.get()
                .uri(getApiURI(GITHUB_INSTALLATIONS_GET + "/498695724"))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(ONLYDUST_ACCOUNT_JSON);
    }
}
