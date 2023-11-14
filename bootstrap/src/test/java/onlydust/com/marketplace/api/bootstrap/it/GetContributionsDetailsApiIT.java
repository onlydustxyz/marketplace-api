package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class GetContributionsDetailsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HasuraUserHelper userHelper;

    @Test
    void should_return_404_when_not_found() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTION_BY_ID,
                        "298a547f-ecb6-4ab2-8975-68f4e9bf7b39", "000000"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void should_return_contribution_details_when_found() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTION_BY_ID,
                        "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                        "b66cd16a35e0043d86f1850eb9ba6519d20ff833394f7516b0842fa2f18a5abf")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                           "id": "b66cd16a35e0043d86f1850eb9ba6519d20ff833394f7516b0842fa2f18a5abf",
                           "createdAt": "2022-07-08T15:07:29Z",
                           "completedAt": "2022-07-08T15:17:51Z",
                           "type": "PULL_REQUEST",
                           "status": "COMPLETED",
                           "repo": {
                             "id": 498695724,
                             "owner": "onlydustxyz",
                             "name": "marketplace-frontend",
                             "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                           },
                           "githubAuthor": {
                             "githubUserId": 34384633,
                             "login": "tdelabro",
                             "htmlUrl": "https://github.com/tdelabro",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4"
                           },
                           "githubNumber": 62,
                           "githubStatus": "MERGED",
                           "githubTitle": "Anthony buisset feature/starknet",
                           "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/62",
                           "githubBody": null,
                           "githubCodeReviewOutcome": null,
                           "project": {
                             "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                             "slug": "kaaper",
                             "name": "kaaper",
                             "shortDescription": "Documentation generator for Cairo projects.",
                             "logoUrl": null,
                             "visibility": "PUBLIC"
                           },
                           "commentsCount": 0,
                           "links": [],
                           "rewards": [
                             {
                               "id": "6587511b-3791-47c6-8430-8f793606c63a",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T08:01:47.616674Z",
                               "processedAt": null
                             },
                             {
                               "id": "0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T08:00:46.580407Z",
                               "processedAt": null
                             },
                             {
                               "id": "335e45a5-7f59-4519-8a12-1addc530214c",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T08:00:18.005344Z",
                               "processedAt": null
                             },
                             {
                               "id": "e9ebbe59-fb74-4a6c-9a51-6d9050412977",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T08:02:53.470663Z",
                               "processedAt": null
                             },
                             {
                               "id": "e33ea956-d2f5-496b-acf9-e2350faddb16",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T08:01:16.850492Z",
                               "processedAt": null
                             },
                             {
                               "id": "dd7d445f-6915-4955-9bae-078173627b05",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T07:59:47.012001Z",
                               "processedAt": null
                             },
                             {
                               "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T07:59:16.657487Z",
                               "processedAt": null
                             },
                             {
                               "id": "95e079c9-609c-4531-8c5c-13217306b299",
                               "currency": "USD",
                               "amount": 1000,
                               "dollarsEquivalent": 1000,
                               "status": "PROCESSING",
                               "from": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "to": {
                                 "id": 43467246,
                                 "login": "AnthonyBuisset",
                                 "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                 "isRegistered": null
                               },
                               "createdAt": "2023-09-20T08:02:18.711143Z",
                               "processedAt": null
                             }
                           ]
                         }
                        """);
    }

    @Test
    void should_return_code_review_details() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTION_BY_ID,
                        "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                        "9e374e732c9017c3fee800d686e02962dd21b69d9a11c4c21517d76fec56b1a1")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "id": "9e374e732c9017c3fee800d686e02962dd21b69d9a11c4c21517d76fec56b1a1",
                          "createdAt": "2022-07-12T11:26:34Z",
                          "completedAt": "2022-07-12T17:32:58Z",
                          "type": "CODE_REVIEW",
                          "status": "COMPLETED",
                          "repo": {
                            "id": 493591124,
                            "owner": "onlydustxyz",
                            "name": "kaaper",
                            "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                          },
                          "githubAuthor": {
                            "githubUserId": 43467246,
                            "login": "AnthonyBuisset",
                            "htmlUrl": "https://github.com/AnthonyBuisset",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                          },
                          "githubNumber": 17,
                          "githubStatus": "APPROVED",
                          "githubTitle": "Feat/view",
                          "githubHtmlUrl": "https://github.com/onlydustxyz/kaaper/pull/17",
                          "githubBody": null,
                          "githubCodeReviewOutcome": "APPROVED",
                          "project": {
                            "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                            "slug": "kaaper",
                            "name": "kaaper",
                            "shortDescription": "Documentation generator for Cairo projects.",
                            "logoUrl": null,
                            "visibility": "PUBLIC"
                          },
                          "commentsCount": 0,
                          "links": [
                            {
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 493591124,
                                "owner": "onlydustxyz",
                                "name": "kaaper",
                                "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                              },
                              "githubAuthor": {
                                "githubUserId": 26416205,
                                "login": "internnos",
                                "htmlUrl": "https://github.com/internnos",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/26416205?v=4"
                              },
                              "githubNumber": 17,
                              "githubStatus": "MERGED",
                              "githubTitle": "Feat/view",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/kaaper/pull/17",
                              "githubBody": null,
                              "githubCodeReviewOutcome": null,
                              "is_mine": false
                            }
                          ],
                          "rewards": []
                        }
                        """)
        ;
    }


    @Test
    void should_return_403_when_not_mine() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTION_BY_ID,
                        "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                        "2eba1ee6dd0e0a3acb2f0d411eea52500eb572b1eec6f2ccf45bc53e8cd77bd6")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}
