package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class MeGetContributedReposApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HasuraUserHelper userHelper;

    @Test
    void should_get_all_my_contributed_repos() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTED_REPOS))
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
                .uri(getApiURI(ME_GET_CONTRIBUTED_REPOS, Map.of(
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
