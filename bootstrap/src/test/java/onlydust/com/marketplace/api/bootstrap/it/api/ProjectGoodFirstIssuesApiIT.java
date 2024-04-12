package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;

import java.util.Map;


public class ProjectGoodFirstIssuesApiIT extends AbstractMarketplaceApiIT {
    private final static String KAAPER = "298a547f-ecb6-4ab2-8975-68f4e9bf7b39";

    @Test
    void should_get_good_first_issues() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_GOOD_FIRST_ISSUES.formatted(KAAPER), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 15,
                          "totalItemNumber": 71,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 2013944953,
                              "number": 1469,
                              "title": "PR num1401",
                              "status": "COMPLETED",
                              "createdAt": "2023-11-28T08:51:20Z",
                              "closedAt": "2023-11-28T08:51:20Z",
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/1469",
                              "body": "desc",
                              "author": {
                                "githubUserId": 136718082,
                                "login": "od-develop",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/136718082?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": "Contributions marketplace backend services",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "commentCount": 0,
                              "labels": []
                            },
                            {
                              "id": 2006200933,
                              "number": 1433,
                              "title": "test-swagger",
                              "status": "COMPLETED",
                              "createdAt": "2023-11-22T12:06:10Z",
                              "closedAt": "2023-11-22T12:06:11Z",
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/1433",
                              "body": "test-swagger",
                              "author": {
                                "githubUserId": 136718082,
                                "login": "od-develop",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/136718082?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": "Contributions marketplace backend services",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "commentCount": 0,
                              "labels": []
                            },
                            {
                              "id": 2006088843,
                              "number": 1432,
                              "title": "test-swagger",
                              "status": "OPEN",
                              "createdAt": "2023-11-22T11:02:39Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/1432",
                              "body": "test-swagger",
                              "author": {
                                "githubUserId": 136718082,
                                "login": "od-develop",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/136718082?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": "Contributions marketplace backend services",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "commentCount": 0,
                              "labels": []
                            },
                            {
                              "id": 2006086885,
                              "number": 1431,
                              "title": "test-swagger",
                              "status": "OPEN",
                              "createdAt": "2023-11-22T11:01:34Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/1431",
                              "body": "test-swagger",
                              "author": {
                                "githubUserId": 136718082,
                                "login": "od-develop",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/136718082?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": "Contributions marketplace backend services",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "commentCount": 0,
                              "labels": []
                            },
                            {
                              "id": 2006076018,
                              "number": 1429,
                              "title": "test-swagger-postman",
                              "status": "OPEN",
                              "createdAt": "2023-11-22T10:55:21Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/1429",
                              "body": null,
                              "author": {
                                "githubUserId": 136718082,
                                "login": "od-develop",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/136718082?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": "Contributions marketplace backend services",
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "commentCount": 0,
                              "labels": []
                            }
                          ]
                        }
                        """);
    }
}
