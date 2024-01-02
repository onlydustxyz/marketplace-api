package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BudgetRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

public class BackOfficeApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ApiKeyAuthenticationService.Config config;
    @Autowired
    BudgetRepository budgetRepository;

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
    void should_raise_missing_authentication_given_no_api_key_when_getting_budgets() {
        // When
        client.get()
                .uri(getApiURI(GET_BUDGETS, Map.of("pageIndex", "0", "pageSize", "5")))
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
                .header("Api-Key", config.getApiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 10,
                          "totalItemNumber": 47,
                          "hasMore": true,
                          "nextPageIndex": 1,
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
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_GITHUB_REPOS, Map.of("pageIndex", "0", "pageSize", "5", "projectIds", "467cb27c" +
                                                                                                         "-9726-4f94" +
                                                                                                         "-818e" +
                                                                                                         "-6aa49bbf5e75,b0f54343-3732-4118-8054-dba40f1ffb85")))
                .header("Api-Key", config.getApiKey())
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
    void should_get_sponsors() {
        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", config.getApiKey())
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
                           "sponsors": [
                             {
                               "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                               "name": "Starknet Foundation",
                               "url": "https://starknet.io",
                               "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                               "projectIds": [
                                 "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                                 "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                               ]
                             },
                             {
                               "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                               "name": "Theodo",
                               "url": null,
                               "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                               "projectIds": [
                                 "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                                 "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                               ]
                             },
                             {
                               "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                               "name": "Coca Cola",
                               "url": null,
                               "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                               "projectIds": [
                                 "7d04163c-4187-4313-8066-61504d34fc56",
                                 "98873240-31df-431a-81dc-7d6fe01143a0"
                               ]
                             },
                             {
                               "id": "58a0a05c-c81e-447c-910f-629817a987b8",
                               "name": "Captain America",
                               "url": "https://www.marvel.com/characters/captain-america-steve-rogers",
                               "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg",
                               "projectIds": [
                                 "45ca43d6-130e-4bf7-9776-2b1eb1dcb782"
                               ]
                             },
                             {
                               "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                               "name": "Coca Colax",
                               "url": "https://www.coca-cola-france.fr/",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg",
                               "projectIds": [
                                 "98873240-31df-431a-81dc-7d6fe01143a0"
                               ]
                             }
                           ]
                         }
                        """);

        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "467cb27c-9726-4f94-818e-6aa49bbf5e75,b0f54343-3732-4118-8054-dba40f1ffb85")
                ))
                .header("Api-Key", config.getApiKey())
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
                              "sponsors": [
                                {
                                  "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                                  "name": "Starknet Foundation",
                                  "url": "https://starknet.io",
                                  "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                                  "projectIds": [
                                    "467cb27c-9726-4f94-818e-6aa49bbf5e75"
                                  ]
                                },
                                {
                                  "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                                  "name": "Theodo",
                                  "url": null,
                                  "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                                  "projectIds": [
                                    "467cb27c-9726-4f94-818e-6aa49bbf5e75"
                                  ]
                                }
                              ]
                            }
                        """);


        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sponsorIds", "eb04a5de-4802-4071-be7b-9007b563d48d,2639563e-4437-4bde-a4f4-654977c0cb39")
                ))
                .header("Api-Key", config.getApiKey())
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
                               "sponsors": [
                                 {
                                   "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                                   "name": "Starknet Foundation",
                                   "url": "https://starknet.io",
                                   "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                                   "projectIds": [
                                     "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                                     "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                                   ]
                                 },
                                 {
                                   "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                                   "name": "Theodo",
                                   "url": null,
                                   "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                                   "projectIds": [
                                     "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                                     "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                                   ]
                                 }
                               ]
                             }
                        """);
    }

    @Test
    void should_get_budgets() {
        // When
        client.get()
                .uri(getApiURI(GET_BUDGETS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", config.getApiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 14,
                          "totalItemNumber": 70,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "budgets": [
                            {
                              "id": "4b702cb1-28d2-49ef-8a7b-48f23ebe5ddd",
                              "currency": "USD",
                              "initialAmount": 1789654,
                              "remainingAmount": 1789654,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 1789654,
                              "initialAmountDollarsEquivalent": 1789654,
                              "spentAmountDollarsEquivalent": 0,
                              "projectId": "ccf90dcf-a91b-42c6-b5ca-49d687b4401a"
                            },
                            {
                              "id": "c3c492f8-e853-44af-a2f5-a413c200dd79",
                              "currency": "USD",
                              "initialAmount": 15000,
                              "remainingAmount": 15000,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 15000,
                              "initialAmountDollarsEquivalent": 15000,
                              "spentAmountDollarsEquivalent": 0,
                              "projectId": "8daa34b4-563a-4ef5-8c1c-4bcffdfbc4f6"
                            },
                            {
                              "id": "6bdc7650-266c-4854-ac20-1073c0218774",
                              "currency": "USD",
                              "initialAmount": 1789654,
                              "remainingAmount": 1789654,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 1789654,
                              "initialAmountDollarsEquivalent": 1789654,
                              "spentAmountDollarsEquivalent": 0,
                              "projectId": "56504731-0398-441f-80ac-90edbd14675f"
                            },
                            {
                              "id": "20254f76-c3e9-4430-b04c-5cb3c715e38b",
                              "currency": "USD",
                              "initialAmount": 10000,
                              "remainingAmount": 10000,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 10000,
                              "initialAmountDollarsEquivalent": 10000,
                              "spentAmountDollarsEquivalent": 0,
                              "projectId": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17"
                            },
                            {
                              "id": "ed9d26bc-8497-49e6-861d-883e4b23b175",
                              "currency": "USD",
                              "initialAmount": 1789654,
                              "remainingAmount": 1789654,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 1789654,
                              "initialAmountDollarsEquivalent": 1789654,
                              "spentAmountDollarsEquivalent": 0,
                              "projectId": "c44930eb-d292-4de0-99b3-85957e1a7a1a"
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_BUDGETS, Map.of("pageIndex", "0", "pageSize", "5", "projectIds",
                        "ccf90dcf-a91b-42c6-b5ca-49d687b4401a,56504731-0398-441f-80ac-90edbd14675f")))
                .header("Api-Key", config.getApiKey())
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
                          "budgets": [
                            {
                              "id": "4b702cb1-28d2-49ef-8a7b-48f23ebe5ddd",
                              "currency": "USD",
                              "initialAmount": 1789654,
                              "remainingAmount": 1789654,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 1789654,
                              "initialAmountDollarsEquivalent": 1789654,
                              "spentAmountDollarsEquivalent": 0,
                              "projectId": "ccf90dcf-a91b-42c6-b5ca-49d687b4401a"
                            },
                            {
                              "id": "6bdc7650-266c-4854-ac20-1073c0218774",
                              "currency": "USD",
                              "initialAmount": 1789654,
                              "remainingAmount": 1789654,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 1789654,
                              "initialAmountDollarsEquivalent": 1789654,
                              "spentAmountDollarsEquivalent": 0,
                              "projectId": "56504731-0398-441f-80ac-90edbd14675f"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_stark_budgets_with_no_usd_equivalent() {
        // Given
        {
            final var budget = budgetRepository.findById(UUID.fromString("7dcf96a0-ea20-4f95-99f4-89cee2bf3911"))
                    .orElseThrow();
            budget.setCurrency(CurrencyEnumEntity.strk);
            budgetRepository.save(budget);
        }

        // When
        client.get()
                .uri(getApiURI(GET_BUDGETS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "02a533f5-6cbb-4cb6-90fe-f6bee220443c"
                )))
                .header("Api-Key", config.getApiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "budgets": [
                            {
                              "id": "7dcf96a0-ea20-4f95-99f4-89cee2bf3911",
                              "currency": "STRK",
                              "initialAmount": 10000,
                              "remainingAmount": 9000,
                              "spentAmount": 1000,
                              "remainingAmountDollarsEquivalent": null,
                              "initialAmountDollarsEquivalent": null,
                              "spentAmountDollarsEquivalent": null,
                              "projectId": "02a533f5-6cbb-4cb6-90fe-f6bee220443c"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_project_lead_invitations() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECT_LEAD_INVITATIONS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", config.getApiKey())
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
                               "id": "b18b3ed7-b4c7-40b7-8d7d-5e6a136374fe",
                               "projectId": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                               "githubUserId": 34384633
                             },
                             {
                               "id": "03d6f190-f898-49fa-a1e5-e6174295d3e8",
                               "projectId": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                               "githubUserId": 117665867
                             },
                             {
                               "id": "da72cae6-839b-4f4f-918c-02c3607aa3a2",
                               "projectId": "45ca43d6-130e-4bf7-9776-2b1eb1dcb782",
                               "githubUserId": 8480969
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
                             }
                           ]
                         }
                        """);

        client.get()
                .uri(getApiURI(GET_PROJECT_LEAD_INVITATIONS, Map.of("pageIndex", "0", "pageSize", "5", "ids",
                        "03d6f190-f898-49fa-a1e5-e6174295d3e8,16def485-d98f-4619-801a-ca147c8c64a6")))
                .header("Api-Key", config.getApiKey())
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
                .header("Api-Key", config.getApiKey())
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
    void should_get_users() {
        // When
        client.get()
                .uri(getApiURI(GET_USERS, Map.of("pageIndex", "0", "pageSize", "3")))
                .header("Api-Key", config.getApiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.users[?(@.updatedAt empty true)]").doesNotExist()
                .jsonPath("$.users[?(@.updatedAt empty false)]").exists()
                .jsonPath("$.users[?(@.lastSeenAt empty true)]").doesNotExist()
                .jsonPath("$.users[?(@.lastSeenAt empty false)]").exists()
                .json("""
                        {
                          "totalPageNumber": 9,
                          "totalItemNumber": 27,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "users": [
                            {
                              "id": "eaa1ddf3-fea5-4cef-825b-336f8e775e05",
                              "isCompany": false,
                              "companyName": null,
                              "companyNum": null,
                              "firstname": null,
                              "lastname": null,
                              "address": null,
                              "postCode": null,
                              "city": null,
                              "country": null,
                              "telegram": null,
                              "twitter": "https://twitter.com/haydencleary",
                              "discord": null,
                              "linkedin": null,
                              "whatsapp": null,
                              "bic": null,
                              "iban": null,
                              "ens": null,
                              "ethAddress": null,
                              "aptosAddress": null,
                              "optimismAddress": null,
                              "starknetAddress": null,
                              "createdAt": "2023-10-03T09:06:07.741395Z",
                              "email": "haydenclearymusic@gmail.com",
                              "githubUserId": 5160414,
                              "githubLogin": "haydencleary",
                              "githubHtmlUrl": "https://github.com/haydencleary",
                              "githubAvatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                              "bio": "Freelance web developer focused on Typescript and React.js",
                              "location": "Limoges, France",
                              "website": "https://haydencleary.com/",
                              "lookingForAJob": null,
                              "weeklyAllocatedTime": null,
                              "languages": [
                                "TypeScript",
                                "JavaScript",
                                "Rust",
                                "Shell",
                                "CSS",
                                "MDX",
                                "HTML",
                                "PLpgSQL",
                                "Dockerfile"
                              ],
                              "tcAcceptedAt": "2023-10-03 09:06:18.066198",
                              "onboardingCompletedAt": "2023-10-03T09:06:13.696252Z"
                            },
                            {
                              "id": "f4af340d-6923-453c-bffe-2f1ce1880ff4",
                              "isCompany": false,
                              "companyName": null,
                              "companyNum": null,
                              "firstname": null,
                              "lastname": null,
                              "address": null,
                              "postCode": null,
                              "city": null,
                              "country": null,
                              "telegram": null,
                              "twitter": null,
                              "discord": null,
                              "linkedin": null,
                              "whatsapp": null,
                              "bic": null,
                              "iban": null,
                              "ens": null,
                              "ethAddress": null,
                              "aptosAddress": null,
                              "optimismAddress": null,
                              "starknetAddress": null,
                              "createdAt": "2023-09-27T12:04:06.149173Z",
                              "email": "admin@onlydust.xyz",
                              "githubUserId": 144809540,
                              "githubLogin": "CamilleOD",
                              "githubHtmlUrl": "https://github.com/CamilleOD",
                              "githubAvatarUrl": "https://avatars.githubusercontent.com/u/144809540?v=4",
                              "bio": null,
                              "location": null,
                              "website": null,
                              "lookingForAJob": null,
                              "weeklyAllocatedTime": null,
                              "languages": null,
                              "tcAcceptedAt": "2023-09-27 12:04:21.032055",
                              "onboardingCompletedAt": "2023-09-27T12:04:17.551646Z"
                            },
                            {
                              "id": "0d29b7bd-9514-4a03-ad14-99bbcbef4733",
                              "isCompany": false,
                              "companyName": null,
                              "companyNum": null,
                              "firstname": null,
                              "lastname": null,
                              "address": null,
                              "postCode": null,
                              "city": null,
                              "country": null,
                              "telegram": null,
                              "twitter": null,
                              "discord": null,
                              "linkedin": null,
                              "whatsapp": null,
                              "bic": null,
                              "iban": null,
                              "ens": null,
                              "ethAddress": null,
                              "aptosAddress": null,
                              "optimismAddress": null,
                              "starknetAddress": null,
                              "createdAt": "2023-09-18T15:45:50.175156Z",
                              "email": "kevin@lettria.com",
                              "githubUserId": 142427301,
                              "githubLogin": "letkev",
                              "githubHtmlUrl": "https://github.com/letkev",
                              "githubAvatarUrl": "https://avatars.githubusercontent.com/u/142427301?v=4",
                              "bio": "",
                              "location": "",
                              "website": "",
                              "lookingForAJob": false,
                              "weeklyAllocatedTime": "none",
                              "languages": null,
                              "tcAcceptedAt": "2023-09-18 15:45:59.929954",
                              "onboardingCompletedAt": "2023-09-18T15:45:56.594259Z"
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_USERS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "3",
                        "userIds", "cde93e0e-99cf-4722-8aaa-2c27b91e270d,747e663f-4e68-4b42-965b-b5aebedcd4c4"

                )))
                .header("Api-Key", config.getApiKey())
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
                          "users": [
                            {
                              "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "isCompany": false,
                              "companyName": null,
                              "companyNum": null,
                              "firstname": "Anthony",
                              "lastname": "BUISSET",
                              "address": "771 chemin de la sine",
                              "postCode": "06140",
                              "city": "Vence",
                              "country": "France",
                              "telegram": "https://t.me/abuisset",
                              "twitter": "https://twitter.com/abuisset",
                              "discord": "antho",
                              "linkedin": "https://www.linkedin.com/in/anthony-buisset/",
                              "whatsapp": null,
                              "bic": null,
                              "iban": null,
                              "ens": "abuisset.eth",
                              "ethAddress": null,
                              "aptosAddress": null,
                              "optimismAddress": null,
                              "starknetAddress": null,
                              "createdAt": "2022-12-12T09:51:58.48559Z",
                              "lastSeenAt": "2023-10-05T19:06:50.034Z",
                              "email": "abuisset@gmail.com",
                              "githubUserId": 43467246,
                              "githubLogin": "AnthonyBuisset",
                              "githubHtmlUrl": "https://github.com/AnthonyBuisset",
                              "githubAvatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                              "bio": "FullStack engineerr",
                              "location": "Vence, France",
                              "website": "https://linktr.ee/abuisset",
                              "lookingForAJob": false,
                              "weeklyAllocatedTime": "none",
                              "languages": ["TypeScript", "Rust", "Python", "Cairo", "HCL", "Nix", "PLpgSQL", "Makefile", "CSS", "JavaScript", "Shell", "Dockerfile", "Procfile", "HTML"],
                              "tcAcceptedAt": "2023-06-16 16:10:53.562624",
                              "onboardingCompletedAt": "2023-06-28T13:43:28.30742Z"
                            },
                            {
                              "id": "cde93e0e-99cf-4722-8aaa-2c27b91e270d",
                              "isCompany": false,
                              "companyName": null,
                              "companyNum": null,
                              "firstname": null,
                              "lastname": null,
                              "address": null,
                              "postCode": null,
                              "city": null,
                              "country": null,
                              "telegram": null,
                              "twitter": "https://twitter.com/OnlyDust_xyz",
                              "discord": null,
                              "linkedin": null,
                              "whatsapp": null,
                              "bic": null,
                              "iban": null,
                              "ens": null,
                              "ethAddress": null,
                              "aptosAddress": null,
                              "optimismAddress": null,
                              "starknetAddress": null,
                              "createdAt": "2023-02-01T08:56:05.771022Z",
                              "lastSeenAt": "2023-02-01T08:56:06.027Z",
                              "email": "tech@onlydust.xyz",
                              "githubUserId": 112474158,
                              "githubLogin": "onlydust-contributor",
                              "githubHtmlUrl": "https://github.com/onlydust-contributor",
                              "githubAvatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                              "bio": "Get paid to contribute to open source projects on OnlyDust.xyz",
                              "location": null,
                              "website": "OnlyDust.xyz",
                              "lookingForAJob": null,
                              "weeklyAllocatedTime": null,
                              "languages": ["TypeScript", "JavaScript", "Shell", "CSS", "MDX", "HTML", "PLpgSQL"],
                              "tcAcceptedAt": null,
                              "onboardingCompletedAt": null
                            }
                          ]
                        }
                        """);
    }


    @Test
    void should_get_payments() {
        // When
        client.get()
                .uri(getApiURI(GET_PAYMENTS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", config.getApiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                           "totalPageNumber": 48,
                           "totalItemNumber": 239,
                           "hasMore": true,
                           "nextPageIndex": 1,
                           "payments": [
                             {
                               "id": "0341317f-b831-412a-9cec-a5a16a9d749c",
                               "budgetId": "cad5d63e-d570-497e-acef-11a57691d589",
                               "projectId": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                               "amount": 1000,
                               "currency": "USD",
                               "recipientId": 8642470,
                               "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                               "items": [
                                 "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                               ],
                               "requestedAt": "2023-05-26T09:30:42.881962Z",
                               "processedAt": "2023-06-19T21:40:42.314436Z",
                               "pullRequestsCount": 1,
                               "issuesCount": 0,
                               "dustyIssuesCount": 0,
                               "codeReviewsCount": 0,
                               "isPayable": true,
                               "payoutSettings": "FR7640618802650004034616528 / BOUSFRPPXXX"
                             },
                             {
                               "id": "047bcb92-dfbf-45c0-970d-509781237b2e",
                               "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                               "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                               "amount": 500,
                               "currency": "USD",
                               "recipientId": 116729712,
                               "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                               "items": [
                                 "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                               ],
                               "requestedAt": "2022-12-23T13:46:18.624338Z",
                               "processedAt": "2022-12-23T13:46:18.666452Z",
                               "pullRequestsCount": 1,
                               "issuesCount": 0,
                               "dustyIssuesCount": 0,
                               "codeReviewsCount": 0,
                               "isPayable": false,
                               "payoutSettings": null
                             },
                             {
                               "id": "061e2c7e-bda4-49a8-9914-2e76926f70c2",
                               "budgetId": "cad5d63e-d570-497e-acef-11a57691d589",
                               "projectId": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                               "amount": 1000,
                               "currency": "USD",
                               "recipientId": 43467246,
                               "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                               "items": [
                                 "https://github.com/od-mocks/cool-repo-A/pull/397"
                               ],
                               "requestedAt": "2023-05-15T12:15:54.25529Z",
                               "processedAt": "2023-07-27T10:27:14.522708Z",
                               "pullRequestsCount": 1,
                               "issuesCount": 0,
                               "dustyIssuesCount": 0,
                               "codeReviewsCount": 0,
                               "isPayable": true,
                               "payoutSettings": "abuisset.eth"
                             },
                             {
                               "id": "079df81a-d9f4-4e46-80cf-c17f400fe88f",
                               "budgetId": "915814c3-981c-4d32-a965-7a3c1dc96fbd",
                               "projectId": "c66b929a-664d-40b9-96c4-90d3efd32a3c",
                               "amount": 438,
                               "currency": "USD",
                               "recipientId": 4435377,
                               "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                               "items": [
                                 "https://github.com/ArkProjectNFTs/ark-lane/pull/54"
                               ],
                               "requestedAt": "2023-02-06T13:55:13.928436Z",
                               "processedAt": null,
                               "pullRequestsCount": 1,
                               "issuesCount": 0,
                               "dustyIssuesCount": 0,
                               "codeReviewsCount": 0,
                               "isPayable": false,
                               "payoutSettings": null
                             },
                             {
                               "id": "07e75bb9-87fc-4a22-9bea-1232c27e56d4",
                               "budgetId": "a419c321-469a-4464-b6e1-56e800b53952",
                               "projectId": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                               "amount": 10,
                               "currency": "USD",
                               "recipientId": 30843220,
                               "requestorId": "6115f024-159a-4b1f-b713-1e2ad5c6063e",
                               "items": [
                                 "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                               ],
                               "requestedAt": "2022-12-23T11:19:28.353471Z",
                               "processedAt": "2022-12-23T11:19:28.395366Z",
                               "pullRequestsCount": 1,
                               "issuesCount": 0,
                               "dustyIssuesCount": 0,
                               "codeReviewsCount": 0,
                               "isPayable": false,
                               "payoutSettings": null
                             }
                           ]
                         }
                        """);

        client.get()
                .uri(getApiURI(GET_PAYMENTS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "8156fc5f-cec5-4f70-a0de-c368772edcd4,6d955622-c1ce-4227-85ea-51cb1b3207b1"
                )))
                .header("Api-Key", config.getApiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "totalPageNumber": 12,
                            "totalItemNumber": 58,
                            "hasMore": true,
                            "nextPageIndex": 1,
                            "payments": [
                              {
                                "id": "047bcb92-dfbf-45c0-970d-509781237b2e",
                                "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                                "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                                "amount": 500,
                                "currency": "USD",
                                "recipientId": 116729712,
                                "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "items": [
                                  "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                                ],
                                "requestedAt": "2022-12-23T13:46:18.624338Z",
                                "processedAt": "2022-12-23T13:46:18.666452Z",
                                "pullRequestsCount": 1,
                                "issuesCount": 0,
                                "dustyIssuesCount": 0,
                                "codeReviewsCount": 0
                              },
                              {
                                "id": "07e75bb9-87fc-4a22-9bea-1232c27e56d4",
                                "budgetId": "a419c321-469a-4464-b6e1-56e800b53952",
                                "projectId": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                "amount": 10,
                                "currency": "USD",
                                "recipientId": 30843220,
                                "requestorId": "6115f024-159a-4b1f-b713-1e2ad5c6063e",
                                "items": [
                                  "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                                ],
                                "requestedAt": "2022-12-23T11:19:28.353471Z",
                                "processedAt": "2022-12-23T11:19:28.395366Z",
                                "pullRequestsCount": 1,
                                "issuesCount": 0,
                                "dustyIssuesCount": 0,
                                "codeReviewsCount": 0
                              },
                              {
                                "id": "0e31bf2e-e73d-41e7-ba6d-d0e608c5a7f9",
                                "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                                "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                                "amount": 3000,
                                "currency": "USD",
                                "recipientId": 4404287,
                                "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "items": [
                                  "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                                ],
                                "requestedAt": "2022-12-23T13:46:17.48837Z",
                                "processedAt": "2022-12-23T13:46:17.527921Z",
                                "pullRequestsCount": 1,
                                "issuesCount": 0,
                                "dustyIssuesCount": 0,
                                "codeReviewsCount": 0
                              },
                              {
                                "id": "1325c03f-348b-48e5-80c9-a589df5fe400",
                                "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                                "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                                "amount": 3000,
                                "currency": "USD",
                                "recipientId": 18620296,
                                "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "items": [
                                  "https://github.com/kkrt-labs/kakarot/pull/248",
                                  "https://github.com/kkrt-labs/kakarot/pull/243",
                                  "https://github.com/kkrt-labs/kakarot/pull/237",
                                  "https://github.com/kkrt-labs/kakarot/pull/242",
                                  "https://github.com/kkrt-labs/kakarot/pull/218"
                                ],
                                "requestedAt": "2022-12-28T08:23:24.765473Z",
                                "processedAt": "2022-12-28T08:23:24.828445Z",
                                "pullRequestsCount": 5,
                                "issuesCount": 0,
                                "dustyIssuesCount": 0,
                                "codeReviewsCount": 0
                              },
                              {
                                "id": "150817bb-8484-4e31-a332-ac8378d0a6e2",
                                "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                                "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                                "amount": 500,
                                "currency": "USD",
                                "recipientId": 116874460,
                                "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "items": [
                                  "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                                ],
                                "requestedAt": "2022-12-23T13:46:21.984512Z",
                                "processedAt": "2022-12-23T13:46:22.024677Z",
                                "pullRequestsCount": 1,
                                "issuesCount": 0,
                                "dustyIssuesCount": 0,
                                "codeReviewsCount": 0
                              }
                            ]
                          }
                        """);


        client.get()
                .uri(getApiURI(GET_PAYMENTS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "paymentIds", "1325c03f-348b-48e5-80c9-a589df5fe400,150817bb-8484-4e31-a332-ac8378d0a6e2"
                )))
                .header("Api-Key", config.getApiKey())
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
                            "payments": [
                              {
                                "id": "1325c03f-348b-48e5-80c9-a589df5fe400",
                                "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                                "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                                "amount": 3000,
                                "currency": "USD",
                                "recipientId": 18620296,
                                "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "items": [
                                  "https://github.com/kkrt-labs/kakarot/pull/248",
                                  "https://github.com/kkrt-labs/kakarot/pull/243",
                                  "https://github.com/kkrt-labs/kakarot/pull/237",
                                  "https://github.com/kkrt-labs/kakarot/pull/242",
                                  "https://github.com/kkrt-labs/kakarot/pull/218"
                                ],
                                "requestedAt": "2022-12-28T08:23:24.765473Z",
                                "processedAt": "2022-12-28T08:23:24.828445Z",
                                "pullRequestsCount": 5,
                                "issuesCount": 0,
                                "dustyIssuesCount": 0,
                                "codeReviewsCount": 0
                              },
                              {
                                "id": "150817bb-8484-4e31-a332-ac8378d0a6e2",
                                "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                                "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                                "amount": 500,
                                "currency": "USD",
                                "recipientId": 116874460,
                                "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "items": [
                                  "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                                ],
                                "requestedAt": "2022-12-23T13:46:21.984512Z",
                                "processedAt": "2022-12-23T13:46:22.024677Z",
                                "pullRequestsCount": 1,
                                "issuesCount": 0,
                                "dustyIssuesCount": 0,
                                "codeReviewsCount": 0
                              }
                            ]
                          }
                        """);
    }

    @Test
    void should_get_projects() {
        // When
        client.get()
                .uri(getApiURI(GET_PROJECTS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", config.getApiKey())
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
                              "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                              "name": "Cairo foundry",
                              "shortDescription": "Foundry like framework for starknet contracts",
                              "longDescription": "",
                              "moreInfoLinks": null,
                              "logoUrl": null,
                              "hiring": false,
                              "rank": 0,
                              "visibility": "PUBLIC",
                              "projectLeads": [
                                "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                              ],
                              "createdAt": "2022-12-15T08:44:06.319513Z"
                            },
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
                              "createdAt": "2023-01-10T13:37:26.545996Z"
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
                              "createdAt": "2023-05-17T14:19:29.07864Z"
                            },
                            {
                              "id": "247ac542-762d-44cb-b8d4-4d6199c916be",
                              "name": "Bretzel 196",
                              "shortDescription": "bretzel gives you wings",
                              "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                              "moreInfoLinks": null,
                              "logoUrl": null,
                              "hiring": true,
                              "rank": 0,
                              "visibility": "PUBLIC",
                              "projectLeads": [
                                "45e98bf6-25c2-4edf-94da-e340daba8964"
                              ],
                              "createdAt": "2023-05-24T13:54:25.002945Z"
                            },
                            {
                              "id": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                              "name": "kaaper2",
                              "shortDescription": "Another kaaper",
                              "longDescription": "",
                              "moreInfoLinks": null,
                              "logoUrl": null,
                              "hiring": false,
                              "rank": 0,
                              "visibility": "PUBLIC",
                              "projectLeads": [
                                "6115f024-159a-4b1f-b713-1e2ad5c6063e",
                                "dd0ab03c-5875-424b-96db-a35522eab365"
                              ],
                              "createdAt": "2022-12-23T13:41:08.693859Z"
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_PROJECTS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54,61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17")))
                .header("Api-Key", config.getApiKey())
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
                              "createdAt": "2023-01-10T13:37:26.545996Z"
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
                              "createdAt": "2023-05-17T14:19:29.07864Z"
                            }
                          ]
                        }
                        """);
    }
}
