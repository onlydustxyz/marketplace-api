package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BudgetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

public class BackOfficeApiIT extends AbstractMarketplaceBackOfficeApiIT {
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                                "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                                "name": "AS Nancy Lorraine",
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png",
                                "projectIds": [
                                  "98873240-31df-431a-81dc-7d6fe01143a0",
                                  "a0c91aee-9770-4000-a893-953ddcbd62a7"
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
                                "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                                "name": "Coca Colax",
                                "url": "https://www.coca-cola-france.fr/",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg",
                                "projectIds": [
                                  "98873240-31df-431a-81dc-7d6fe01143a0"
                                ]
                              },
                              {
                                "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                                "name": "OGC Nissa Ineos",
                                "url": "https://www.ogcnice.com/fr/",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png",
                                "projectIds": [
                                  "98873240-31df-431a-81dc-7d6fe01143a0",
                                  "97f6b849-1545-4064-83f1-bc5ded33a8b3",
                                  "a0c91aee-9770-4000-a893-953ddcbd62a7",
                                  "7d04163c-4187-4313-8066-61504d34fc56"
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 16,
                          "totalItemNumber": 78,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "budgets": [
                            {
                              "id": "04c100a0-2f97-452a-8ebc-04ee64e44be9",
                              "currency": "USDC",
                              "initialAmount": 20000,
                              "remainingAmount": 20000,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 20200.00,
                              "initialAmountDollarsEquivalent": 20200.00,
                              "spentAmountDollarsEquivalent": 0.00,
                              "projectId": "b0f54343-3732-4118-8054-dba40f1ffb85"
                            },
                            {
                              "id": "08f002e2-c954-4a4b-8898-e5e717281fd3",
                              "currency": "USDC",
                              "initialAmount": 106250.00,
                              "remainingAmount": 99250.00,
                              "spentAmount": 7000.00,
                              "remainingAmountDollarsEquivalent": 100242.5000,
                              "initialAmountDollarsEquivalent": 107312.5000,
                              "spentAmountDollarsEquivalent": 7070.0000,
                              "projectId": "7d04163c-4187-4313-8066-61504d34fc56"
                            },
                            {
                              "id": "0b3478c2-3ff5-4943-86c3-cda5aa0e0aaf",
                              "currency": "USDC",
                              "initialAmount": 100000,
                              "remainingAmount": 100000,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 101000.00,
                              "initialAmountDollarsEquivalent": 101000.00,
                              "spentAmountDollarsEquivalent": 0.00,
                              "projectId": "00490be6-2c03-4720-993b-aea3e07edd81"
                            },
                            {
                              "id": "1762565f-f9d9-4f0e-8bc3-65df00a7e1e5",
                              "currency": "USDC",
                              "initialAmount": 66666,
                              "remainingAmount": 62166,
                              "spentAmount": 4500,
                              "remainingAmountDollarsEquivalent": 62787.66,
                              "initialAmountDollarsEquivalent": 67332.66,
                              "spentAmountDollarsEquivalent": 4545.00,
                              "projectId": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                            },
                            {
                              "id": "1775ca35-333a-456d-b9d3-7c8fd1890fa2",
                              "currency": "USDC",
                              "initialAmount": 50000,
                              "remainingAmount": 50000,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 50500.00,
                              "initialAmountDollarsEquivalent": 50500.00,
                              "spentAmountDollarsEquivalent": 0.00,
                              "projectId": "27ca7e18-9e71-468f-8825-c64fe6b79d66"
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_BUDGETS, Map.of("pageIndex", "0", "pageSize", "5", "projectIds",
                        "ccf90dcf-a91b-42c6-b5ca-49d687b4401a,56504731-0398-441f-80ac-90edbd14675f")))
                .header("Api-Key", apiKey())
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
                              "currency": "USDC",
                              "initialAmount": 1789654,
                              "remainingAmount": 1789654,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 1807550.54,
                              "initialAmountDollarsEquivalent": 1807550.54,
                              "spentAmountDollarsEquivalent": 0.00,
                              "projectId": "ccf90dcf-a91b-42c6-b5ca-49d687b4401a"
                            },
                            {
                              "id": "6bdc7650-266c-4854-ac20-1073c0218774",
                              "currency": "USDC",
                              "initialAmount": 1789654,
                              "remainingAmount": 1789654,
                              "spentAmount": 0,
                              "remainingAmountDollarsEquivalent": 1807550.54,
                              "initialAmountDollarsEquivalent": 1807550.54,
                              "spentAmountDollarsEquivalent": 0.00,
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                              "id": "5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c",
                              "budgetId": "ce2929d8-01eb-42d6-8285-89a1e6eea3d0",
                              "projectId": "7d04163c-4187-4313-8066-61504d34fc56",
                              "amount": 1000.00,
                              "currency": "USD",
                              "recipientId": 8642470,
                              "isPayable": true,
                              "payoutSettings": "FR7640618802650004034616528 / BOUSFRPPXXX",
                              "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                              "items": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "requestedAt": "2023-10-08T10:09:31.842962Z",
                              "processedAt": null,
                              "pullRequestsCount": 1,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "fab7aaf4-9b0c-4e52-bc9b-72ce08131617",
                              "budgetId": "ce2929d8-01eb-42d6-8285-89a1e6eea3d0",
                              "projectId": "7d04163c-4187-4313-8066-61504d34fc56",
                              "amount": 1000.00,
                              "currency": "USD",
                              "recipientId": 8642470,
                              "isPayable": true,
                              "payoutSettings": "FR7640618802650004034616528 / BOUSFRPPXXX",
                              "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                              "items": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "requestedAt": "2023-10-08T10:06:42.730697Z",
                              "processedAt": null,
                              "pullRequestsCount": 1,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "64fb2732-5632-4b09-a8b1-217485648129",
                              "budgetId": "ce2929d8-01eb-42d6-8285-89a1e6eea3d0",
                              "projectId": "7d04163c-4187-4313-8066-61504d34fc56",
                              "amount": 1000.00,
                              "currency": "USD",
                              "recipientId": 8642470,
                              "isPayable": true,
                              "payoutSettings": "FR7640618802650004034616528 / BOUSFRPPXXX",
                              "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                              "items": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "requestedAt": "2023-10-08T10:00:31.105159Z",
                              "processedAt": null,
                              "pullRequestsCount": 1,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "736e0554-f30e-4315-9731-7611fa089dcf",
                              "budgetId": "08f002e2-c954-4a4b-8898-e5e717281fd3",
                              "projectId": "7d04163c-4187-4313-8066-61504d34fc56",
                              "amount": 1000.00,
                              "currency": "USDC",
                              "recipientId": 8642470,
                              "isPayable": false,
                              "payoutSettings": null,
                              "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                              "items": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "requestedAt": "2023-09-26T15:57:29.834949Z",
                              "processedAt": "2023-09-26T21:08:01.957813Z",
                              "pullRequestsCount": 1,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "1c56d096-5284-4ae3-af3c-dd2b3211fb73",
                              "budgetId": "08f002e2-c954-4a4b-8898-e5e717281fd3",
                              "projectId": "7d04163c-4187-4313-8066-61504d34fc56",
                              "amount": 1000.00,
                              "currency": "USDC",
                              "recipientId": 8642470,
                              "isPayable": false,
                              "payoutSettings": null,
                              "requestorId": "45e98bf6-25c2-4edf-94da-e340daba8964",
                              "items": [
                                "https://github.com/MaximeBeasse/KeyDecoder/pull/1"
                              ],
                              "requestedAt": "2023-09-26T08:43:36.823851Z",
                              "processedAt": "2023-09-26T21:08:01.735916Z",
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
                        "projectIds", "8156fc5f-cec5-4f70-a0de-c368772edcd4,6d955622-c1ce-4227-85ea-51cb1b3207b1"
                )))
                .header("Api-Key", apiKey())
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
                              "id": "3310dd67-3d3f-456a-af1d-9b881d173af7",
                              "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                              "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                              "amount": 1000,
                              "currency": "USDC",
                              "recipientId": 21149076,
                              "isPayable": true,
                              "payoutSettings": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea",
                              "requestorId": "dd0ab03c-5875-424b-96db-a35522eab365",
                              "items": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/737"
                              ],
                              "requestedAt": "2023-02-27T12:12:31.513655Z",
                              "processedAt": null,
                              "pullRequestsCount": 1,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "46b09548-5c6f-4e78-b314-8310d5e66c45",
                              "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                              "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                              "amount": 300,
                              "currency": "USDC",
                              "recipientId": 37303126,
                              "isPayable": false,
                              "payoutSettings": null,
                              "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "items": [
                                "https://github.com/kkrt-labs/kakarot/pull/288"
                              ],
                              "requestedAt": "2022-12-28T08:23:32.732114Z",
                              "processedAt": "2022-12-28T08:23:32.785911Z",
                              "pullRequestsCount": 1,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "c3299729-583f-4028-b783-a679e2148d43",
                              "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                              "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                              "amount": 500,
                              "currency": "USDC",
                              "recipientId": 30843220,
                              "isPayable": false,
                              "payoutSettings": null,
                              "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "items": [
                                "https://github.com/kkrt-labs/kakarot/pull/141",
                                "https://github.com/kkrt-labs/kakarot/pull/78"
                              ],
                              "requestedAt": "2022-12-28T08:23:30.79711Z",
                              "processedAt": "2022-12-28T08:23:30.85192Z",
                              "pullRequestsCount": 2,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "74afd7bd-893b-4fed-a55f-24077b4fc44a",
                              "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                              "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                              "amount": 500,
                              "currency": "USDC",
                              "recipientId": 116729712,
                              "isPayable": false,
                              "payoutSettings": null,
                              "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "items": [
                                "https://github.com/kkrt-labs/kakarot/pull/167"
                              ],
                              "requestedAt": "2022-12-28T08:23:30.187422Z",
                              "processedAt": "2022-12-28T08:23:30.248947Z",
                              "pullRequestsCount": 1,
                              "issuesCount": 0,
                              "dustyIssuesCount": 0,
                              "codeReviewsCount": 0
                            },
                            {
                              "id": "3232aacd-859d-4e1e-bd93-52597e978845",
                              "budgetId": "51c40f51-f1e4-43c1-8d2f-8de97e51dca5",
                              "projectId": "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                              "amount": 1000,
                              "currency": "USDC",
                              "recipientId": 7861901,
                              "isPayable": false,
                              "payoutSettings": null,
                              "requestorId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "items": [
                                "https://github.com/kkrt-labs/kakarot/pull/45",
                                "https://github.com/kkrt-labs/kakarot/pull/63",
                                "https://github.com/kkrt-labs/kakarot/pull/48",
                                "https://github.com/kkrt-labs/kakarot/pull/163"
                              ],
                              "requestedAt": "2022-12-28T08:23:28.53449Z",
                              "processedAt": "2022-12-28T08:23:28.587818Z",
                              "pullRequestsCount": 4,
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
                .header("Api-Key", apiKey())
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
                                 "currency": "USDC",
                                 "recipientId": 18620296,
                                 "isPayable": false,
                                 "payoutSettings": null,
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
                                 "currency": "USDC",
                                 "recipientId": 116874460,
                                 "isPayable": false,
                                 "payoutSettings": null,
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
                .header("Api-Key", apiKey())
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
                              "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                              "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                              "moreInfoLinks": null,
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                              "hiring": true,
                              "rank": 1044,
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
                              "rank": 1039,
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
                              "rank": 0,
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
                              "rank": 1044,
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
                              "rank": 1593,
                              "visibility": "PRIVATE",
                              "projectLeads": [
                                "83612081-949a-47c4-a467-6f28f6adad6d"
                              ],
                              "createdAt": "2023-05-24T09:55:04.729065Z",
                              "activeContributors": 2,
                              "newContributors": 0,
                              "uniqueRewardedContributors": 0,
                              "openedIssues": 0,
                              "contributions": 408,
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
                .header("Api-Key", apiKey())
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
}
