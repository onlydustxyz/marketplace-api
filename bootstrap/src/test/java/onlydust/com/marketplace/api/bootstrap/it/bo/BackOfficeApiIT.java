package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.EcosystemRequest;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoEcosystemQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoEcosystemRepository;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.Map;

public class BackOfficeApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    BoEcosystemRepository ecosystemRepository;

    UserAuthHelper.AuthenticatedBackofficeUser pierre;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER));
    }

    @Test
    void should_raise_missing_authentication_given_no_api_key_when_getting_github_repos() {
        // When
        client.get()
                .uri(getApiURI(GET_GITHUB_REPOS, Map.of("pageIndex", "0", "pageSize", "5")))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_raise_missing_authentication_given_no_api_key_when_getting_project_lead_invitations() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECT_LEAD_INVITATIONS, Map.of("pageIndex", "0", "pageSize", "5")))
                .exchange()
                .expectStatus()
                .isUnauthorized();
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
                          "totalItemNumber": 8,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                              "projectIds": [
                                "7d04163c-4187-4313-8066-61504d34fc56"
                              ]
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "projectIds": [
                                "90fb751a-1137-4815-b3c4-54927a5db059"
                              ]
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "projectIds": [
                                "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                              ]
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                              "projectIds": [
                                "7d04163c-4187-4313-8066-61504d34fc56"
                              ]
                            },
                            {
                              "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                              "name": "Lava",
                              "url": "https://www.lavanet.xyz/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg",
                              "projectIds": [
                                "27ca7e18-9e71-468f-8825-c64fe6b79d66"
                              ]
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_ECOSYSTEMS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "7d04163c-4187-4313-8066-61504d34fc56,1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e")
                ))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                               "totalPageNumber": 1,
                               "totalItemNumber": 4,
                               "hasMore": false,
                               "nextPageIndex": 0,
                               "ecosystems": [
                                 {
                                   "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                   "name": "Aptos",
                                   "url": "https://aptosfoundation.org/",
                                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                   "projectIds": [
                                     "7d04163c-4187-4313-8066-61504d34fc56"
                                   ]
                                 },
                                 {
                                   "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                   "name": "Ethereum",
                                   "url": "https://ethereum.foundation/",
                                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                                   "projectIds": [
                                     "7d04163c-4187-4313-8066-61504d34fc56"
                                   ]
                                 },
                                 {
                                   "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                   "name": "Starknet",
                                   "url": "https://www.starknet.io/en",
                                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                   "projectIds": [
                                     "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e"
                                   ]
                                 },
                                 {
                                   "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                   "name": "Zama",
                                   "url": "https://www.zama.ai/",
                                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                                   "projectIds": [
                                     "7d04163c-4187-4313-8066-61504d34fc56"
                                   ]
                                 }
                               ]
                             }
                        """);

        final Page<BoEcosystemQueryEntity> ecosystems = ecosystemRepository.findAll(List.of(), List.of(), Pageable.ofSize(100));
        final BoEcosystemQueryEntity zama = ecosystems.stream().filter(e -> e.getName().equals("Zama")).findFirst().orElseThrow();

        client.get()
                .uri(getApiURI(GET_ECOSYSTEMS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "ecosystemIds", zama.getId().toString())
                ))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].id").isEqualTo(zama.getId().toString())
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].url").isEqualTo(zama.getUrl())
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].logoUrl").isEqualTo(zama.getLogoUrl())
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].projectIds.length()").isEqualTo(zama.getProjectIds().size())
                .jsonPath("$.ecosystems[?(@.name == 'Zama')].projectIds.[0]").isEqualTo(zama.getProjectIds().get(0).toString());
    }

    @Test
    void should_post_ecosystems() {
        // Given
        final EcosystemRequest ecosystemRequest = new EcosystemRequest();
        ecosystemRequest.setLogoUrl(faker.internet().url());
        ecosystemRequest.setUrl(faker.internet().url());
        ecosystemRequest.setName(faker.rickAndMorty().character());
        ecosystemRequest.setDescription(faker.rickAndMorty().quote());

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
                .jsonPath("$.name").isEqualTo(ecosystemRequest.getName())
                .jsonPath("$.slug").isNotEmpty()
                .jsonPath("$.url").isEqualTo(ecosystemRequest.getUrl())
                .jsonPath("$.logoUrl").isEqualTo(ecosystemRequest.getLogoUrl())
                .jsonPath("$.description").isEqualTo(ecosystemRequest.getDescription())
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    void should_search_projects_by_name() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECTS, Map.of("pageIndex", "0", "pageSize", "5", "search", "Du")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projects[?(@.name =~ /.*du.*/i)]").isNotEmpty()
                .jsonPath("$.projects[?(!(@.name =~ /.*du.*/i))]").isEmpty()
        ;
    }
}
