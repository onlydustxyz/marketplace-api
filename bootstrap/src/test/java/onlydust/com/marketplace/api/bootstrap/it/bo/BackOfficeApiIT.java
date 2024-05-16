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
    void should_get_github_repositories() {
        // When
        client.get()
                .uri(getApiURI(GET_GITHUB_REPOS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 9,
                          "totalItemNumber": 41,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "githubRepositories": [
                            {
                              "id": 40652912,
                              "owner": "IonicaBizau",
                              "name": "node-cobol",
                              "technologies": {
                                "COBOL": 10808,
                                "JavaScript": 6987
                              },
                              "projectId": "a852e8fd-de3c-4a14-813e-4b592af40d54"
                            },
                            {
                              "id": 659718526,
                              "owner": "KasarLabs",
                              "name": "deoxys-telemetry",
                              "technologies": {
                                "CSS": 33120,
                                "HTML": 707,
                                "Rust": 408641,
                                "Shell": 732,
                                "Dockerfile": 1982,
                                "JavaScript": 3843,
                                "TypeScript": 190809
                              },
                              "projectId": "7d04163c-4187-4313-8066-61504d34fc56"
                            },
                            {
                              "id": 276180695,
                              "owner": "MaximeBeasse",
                              "name": "KeyDecoder",
                              "technologies": {
                                "C++": 2226,
                                "Dart": 121265,
                                "CMake": 460,
                                "Swift": 404,
                                "Kotlin": 1381,
                                "Objective-C": 38
                              },
                              "projectId": "b0f54343-3732-4118-8054-dba40f1ffb85"
                            },
                            {
                              "id": 276180695,
                              "owner": "MaximeBeasse",
                              "name": "KeyDecoder",
                              "technologies": {
                                "C++": 2226,
                                "Dart": 121265,
                                "CMake": 460,
                                "Swift": 404,
                                "Kotlin": 1381,
                                "Objective-C": 38
                              },
                              "projectId": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                            },
                            {
                              "id": 64700934,
                              "owner": "OpenZeppelin",
                              "name": "openzeppelin-contracts",
                              "technologies": {
                                "Ruby": 255376,
                                "Shell": 8324,
                                "Python": 6719,
                                "Makefile": 1714,
                                "Solidity": 837904,
                                "JavaScript": 1027177
                              },
                              "projectId": "467cb27c-9726-4f94-818e-6aa49bbf5e75"
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_GITHUB_REPOS, Map.of("pageIndex", "0", "pageSize", "5", "projectIds", "467cb27c" +
                        "-9726-4f94" +
                        "-818e" +
                        "-6aa49bbf5e75,b0f54343-3732-4118-8054-dba40f1ffb85")))
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
                          "githubRepositories": [
                            {
                              "id": 64700934,
                              "owner": "OpenZeppelin",
                              "name": "openzeppelin-contracts",
                              "technologies": {
                                "Ruby": 255376,
                                "Shell": 8324,
                                "Python": 6719,
                                "Makefile": 1714,
                                "Solidity": 837904,
                                "JavaScript": 1027177
                              },
                              "projectId": "467cb27c-9726-4f94-818e-6aa49bbf5e75"
                            },
                            {
                              "id": 302082426,
                              "owner": "gregcha",
                              "name": "crew-app",
                              "technologies": {
                                "CSS": 323507,
                                "HTML": 169898,
                                "SCSS": 102453,
                                "JavaScript": 58624
                              },
                              "projectId": "b0f54343-3732-4118-8054-dba40f1ffb85"
                            },
                            {
                              "id": 64700934,
                              "owner": "OpenZeppelin",
                              "name": "openzeppelin-contracts",
                              "technologies": {
                                "Ruby": 255376,
                                "Shell": 8324,
                                "Python": 6719,
                                "Makefile": 1714,
                                "Solidity": 837904,
                                "JavaScript": 1027177
                              },
                              "projectId": "b0f54343-3732-4118-8054-dba40f1ffb85"
                            },
                            {
                              "id": 276180695,
                              "owner": "MaximeBeasse",
                              "name": "KeyDecoder",
                              "technologies": {
                                "C++": 2226,
                                "Dart": 121265,
                                "CMake": 460,
                                "Swift": 404,
                                "Kotlin": 1381,
                                "Objective-C": 38
                              },
                              "projectId": "b0f54343-3732-4118-8054-dba40f1ffb85"
                            }
                          ]
                        }
                                                
                        """);
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
                .jsonPath("$.url").isEqualTo(ecosystemRequest.getUrl())
                .jsonPath("$.logoUrl").isEqualTo(ecosystemRequest.getLogoUrl())
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    void should_get_project_lead_invitations() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECT_LEAD_INVITATIONS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 3,
                          "totalItemNumber": 12,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "project_lead_invitations": [
                            {
                              "id": "02615584-4ff6-4f82-82f7-0e136b676310",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "githubUserId": 98735421
                            },
                            {
                              "id": "03d6f190-f898-49fa-a1e5-e6174295d3e8",
                              "projectId": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                              "githubUserId": 117665867
                            },
                            {
                              "id": "16def485-d98f-4619-801a-ca147c8c64a6",
                              "projectId": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                              "githubUserId": 595505
                            },
                            {
                              "id": "32831076-613a-4c10-9f04-ef6fbcff5e63",
                              "projectId": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                              "githubUserId": 134487694
                            },
                            {
                              "id": "3678b24e-59a9-4b9f-9db3-0f4096ca21be",
                              "projectId": "62565a49-67d8-45fa-9877-7ba8004b2db7",
                              "githubUserId": 98735421
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_PROJECT_LEAD_INVITATIONS, Map.of("pageIndex", "0", "pageSize", "5", "ids",
                        "03d6f190-f898-49fa-a1e5-e6174295d3e8,16def485-d98f-4619-801a-ca147c8c64a6")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "project_lead_invitations": [
                            {
                              "id": "03d6f190-f898-49fa-a1e5-e6174295d3e8",
                              "projectId": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                              "githubUserId": 117665867
                            },
                            {
                              "id": "16def485-d98f-4619-801a-ca147c8c64a6",
                              "projectId": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                              "githubUserId": 595505
                            }
                          ]
                        }
                                                
                        """);

        client.get()
                .uri(getApiURI(GET_PROJECT_LEAD_INVITATIONS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54,298a547f-ecb6-4ab2-8975-68f4e9bf7b39")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "project_lead_invitations": [
                            {
                              "id": "03d6f190-f898-49fa-a1e5-e6174295d3e8",
                              "projectId": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                              "githubUserId": 117665867
                            },
                            {
                              "id": "16def485-d98f-4619-801a-ca147c8c64a6",
                              "projectId": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                              "githubUserId": 595505
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_projects_old() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECTS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projects[?(@.rank empty true)]").doesNotExist()
                .json("""
                        {
                          "totalPageNumber": 15,
                          "totalItemNumber": 75,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "projects": [
                            {
                              "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                              "name": "Aiolia du Lion",
                              "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                              "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                              "moreInfoLinks": null,
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                              "hiring": true,
                              "visibility": "PUBLIC",
                              "projectLeads": [
                                "f2215429-83c7-49ce-954b-66ed453c3315"
                              ],
                              "createdAt": "2023-02-22T12:23:03.403816Z",
                              "activeContributors": 0,
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "contributions": 0,
                              "dollarsEquivalentAmountSent": 0,
                              "strkAmountSent": 0
                            },
                            {
                              "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                              "name": "Aldébaran du Taureau",
                              "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                              "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                              "moreInfoLinks": null,
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed",
                              "hiring": false,
                              "visibility": "PUBLIC",
                              "projectLeads": [
                                "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "f2215429-83c7-49ce-954b-66ed453c3315"
                              ],
                              "createdAt": "2023-02-22T11:26:12.845767Z",
                              "activeContributors": 0,
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "contributions": 0,
                              "dollarsEquivalentAmountSent": 0,
                              "strkAmountSent": 0
                            },
                            {
                              "id": "97f6b849-1545-4064-83f1-bc5ded33a8b3",
                              "name": "Anthology project",
                              "shortDescription": "A very cool project lead by Antho",
                              "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                              "moreInfoLinks": null,
                              "logoUrl": "https://cdn.filestackcontent.com/pgjvFWS8Teq2Yns89IKg",
                              "hiring": false,
                              "visibility": "PUBLIC",
                              "projectLeads": null,
                              "createdAt": "2023-02-27T11:42:18.77154Z",
                              "activeContributors": 0,
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "contributions": 0,
                              "dollarsEquivalentAmountSent": 0,
                              "strkAmountSent": 0
                            },
                            {
                              "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                              "name": "Apibara",
                              "shortDescription": "Listen to starknet events using gRPC and build your own node",
                              "longDescription": "",
                              "moreInfoLinks": null,
                              "logoUrl": null,
                              "hiring": false,
                              "visibility": "PUBLIC",
                              "projectLeads": [
                                "45e98bf6-25c2-4edf-94da-e340daba8964"
                              ],
                              "createdAt": "2023-01-02T11:01:23.107983Z",
                              "activeContributors": 0,
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "contributions": 0,
                              "dollarsEquivalentAmountSent": 0,
                              "strkAmountSent": 0
                            },
                            {
                              "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                              "name": "B Conseil",
                              "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                              "longDescription": "Nous sommes **pure player** du financement et du management de l’innovation. Avec une présence physique à Paris nous adressons des entreprises **sur tout le territoire** jusque dans les départements d'outre mer.  \\nNotre équipe d’**ingénieurs pluridisciplinaire** (École des Mines, Arts et métiers, Centrale Nantes, École centrale d’Electronique, Polytech, Epitech, etc.) nous permet d’adresser **tous les secteurs de l’Innovation et de la recherche**. Nous avons également une parfaite maîtrise de la valorisation des sciences humaines et sociales.",
                              "moreInfoLinks": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                              "hiring": true,
                              "visibility": "PRIVATE",
                              "projectLeads": [
                                "83612081-949a-47c4-a467-6f28f6adad6d"
                              ],
                              "createdAt": "2023-05-24T09:55:04.729065Z",
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "dollarsEquivalentAmountSent": 0,
                              "strkAmountSent": 0
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_PROJECTS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54,61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "projects": [
                            {
                              "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                              "name": "Cairo streams",
                              "shortDescription": "Stream library in cairo",
                              "longDescription": "",
                              "moreInfoLinks": null,
                              "logoUrl": null,
                              "hiring": false,
                              "rank": 0,
                              "visibility": "PUBLIC",
                              "projectLeads": null,
                              "createdAt": "2023-01-10T13:37:26.545996Z",
                              "activeContributors": 0,
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "contributions": 0,
                              "dollarsEquivalentAmountSent": 0,
                              "strkAmountSent": 0
                            },
                            {
                              "id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                              "name": "DogGPT",
                              "shortDescription": "Chat GPT for cat lovers",
                              "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                              "moreInfoLinks": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15366926246018901574.jpg",
                              "hiring": false,
                              "rank": 0,
                              "visibility": "PUBLIC",
                              "projectLeads": null,
                              "createdAt": "2023-05-17T14:19:29.07864Z",
                              "activeContributors": 0,
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "contributions": 0,
                              "dollarsEquivalentAmountSent": 0,
                              "strkAmountSent": 0
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_projects() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECTS_V2, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 15,
                          "totalItemNumber": 75,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "projects": [
                            {
                              "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                              "name": "Aiolia du Lion",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c"
                            },
                            {
                              "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                              "name": "Aldébaran du Taureau",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed"
                            },
                            {
                              "id": "97f6b849-1545-4064-83f1-bc5ded33a8b3",
                              "name": "Anthology project",
                              "logoUrl": "https://cdn.filestackcontent.com/pgjvFWS8Teq2Yns89IKg"
                            },
                            {
                              "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                              "name": "Apibara",
                              "logoUrl": null
                            },
                            {
                              "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                              "name": "B Conseil",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_search_projects_by_name() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECTS_V2, Map.of("pageIndex", "0", "pageSize", "5", "search", "Du")))
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
