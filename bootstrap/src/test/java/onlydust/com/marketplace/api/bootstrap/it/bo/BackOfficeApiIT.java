package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class BackOfficeApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ApiKeyAuthenticationService.Config config;

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
    }


}
