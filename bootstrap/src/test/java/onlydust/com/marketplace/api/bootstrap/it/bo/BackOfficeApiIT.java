package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class BackOfficeApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ApiKeyAuthenticationService.Config config;

    @Test
    void should_raise_missing_authentication_given_no_api_key() {
        // When
        client.get()
                .uri(getApiURI(GET_GITHUB_REPO, Map.of("pageIndex", "0", "pageSize", "5")))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_get_github_repositories() {
        // When
        client.get()
                .uri(getApiURI(GET_GITHUB_REPO, Map.of("pageIndex", "0", "pageSize", "5")))
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
                                "Solidity": 836789,
                                "JavaScript": 1035006
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
                                "Solidity": 836789,
                                "JavaScript": 1035006
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
                .uri(getApiURI(GET_GITHUB_REPO, Map.of("pageIndex", "0", "pageSize", "5", "projectIds","467cb27c-9726-4f94-818e-6aa49bbf5e75,b0f54343-3732-4118-8054-dba40f1ffb85")))
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
                                "Solidity": 836789,
                                "JavaScript": 1035006
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
                                "Solidity": 836789,
                                "JavaScript": 1035006
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
}
