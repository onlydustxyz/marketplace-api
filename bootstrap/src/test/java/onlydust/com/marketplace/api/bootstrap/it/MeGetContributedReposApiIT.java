package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.bootstrap.it.extension.PostgresITExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"hasura_auth", "it"})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(PostgresITExtension.class)
public class MeGetContributedReposApiIT extends AbstractMarketplaceApiIT {
    @LocalServerPort
    int port;
    @Autowired
    WebTestClient client;
    @Autowired
    HasuraUserHelper userHelper;

    @Test
    void should_get_all_my_contributed_repos() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(port, ME_GET_CONTRIBUTED_REPOS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                           "repos": [
                             {
                               "id": 466482535,
                               "owner": "gregcha",
                               "name": "bretzel-ressources",
                               "htmlUrl": "https://github.com/gregcha/bretzel-ressources"
                             },
                             {
                               "id": 480776993,
                               "owner": "onlydustxyz",
                               "name": "starklings",
                               "htmlUrl": "https://github.com/onlydustxyz/starklings"
                             },
                             {
                               "id": 493591124,
                               "owner": "onlydustxyz",
                               "name": "kaaper",
                               "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                             },
                             {
                               "id": 498695724,
                               "owner": "onlydustxyz",
                               "name": "marketplace-frontend",
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                             },
                             {
                               "id": 593701982,
                               "owner": "onlydustxyz",
                               "name": "gateway",
                               "htmlUrl": "https://github.com/onlydustxyz/gateway"
                             },
                             {
                               "id": 602953043,
                               "owner": "od-mocks",
                               "name": "cool-repo-A",
                               "htmlUrl": "https://github.com/od-mocks/cool-repo-A"
                             }
                           ]
                         }
                        """);
    }

    @Test
    void should_get_my_contributed_repos_filtered_by_project() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(port, ME_GET_CONTRIBUTED_REPOS, Map.of(
                        "projects", "f39b827f-df73-498c-8853-99bc3f562723,594ca5ca-48f7-49a8-9c26-84b949d4fdd9")
                ))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                           "repos": [
                             {
                               "id": 498695724,
                               "owner": "onlydustxyz",
                               "name": "marketplace-frontend",
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                             },
                             {
                               "id": 593701982,
                               "owner": "onlydustxyz",
                               "name": "gateway",
                               "htmlUrl": "https://github.com/onlydustxyz/gateway"
                             }
                           ]
                         }
                        """);
    }
}
