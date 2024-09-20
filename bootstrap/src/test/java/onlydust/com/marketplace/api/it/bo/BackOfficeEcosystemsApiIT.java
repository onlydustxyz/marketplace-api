package onlydust.com.marketplace.api.it.bo;

import onlydust.com.backoffice.api.contract.model.EcosystemRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemRepository;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.Map;

@TagBO
public class BackOfficeEcosystemsApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    EcosystemRepository ecosystemRepository;

    UserAuthHelper.AuthenticatedBackofficeUser pierre;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER));
    }

    @Test
    void should_get_ecosystems() {
        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEMS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 9,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "slug": "aptos",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                              "hidden": true,
                              "leads": []
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "slug": "avail",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "hidden": true,
                              "leads": []
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "slug": "aztec",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "hidden": false,
                              "leads": []
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "slug": "ethereum",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                              "hidden": false,
                              "leads": []
                            },
                            {
                              "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                              "slug": "lava",
                              "name": "Lava",
                              "url": "https://www.lavanet.xyz/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg",
                              "hidden": true,
                              "leads": []
                            }
                          ]
                        }
                        """);

        final var zama = ecosystemRepository.findAll().stream().filter(e -> e.getName().equals("Zama")).findFirst().orElseThrow();

        client.get()
                .uri(getApiURI(GET_ECOSYSTEMS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "search", "zama")
                ))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].id").isEqualTo(zama.getId().toString())
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].url").isEqualTo(zama.getUrl())
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].logoUrl").isEqualTo(zama.getLogoUrl());
    }

    @Test
    void should_create_ecosystem() {
        // Given
        final EcosystemRequest ecosystemRequest = new EcosystemRequest()
                .logoUrl(faker.internet().url())
                .url(faker.internet().url())
                .name("Z" + faker.rickAndMorty().character())
                .description(faker.rickAndMorty().quote())
                .hidden(false);

        // When
        client.post()
                .uri(getApiURI(GET_ECOSYSTEMS))
                .header("Authorization", "Bearer " + pierre.jwt())
                .body(BodyInserters.fromValue(ecosystemRequest))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty();
    }
}
