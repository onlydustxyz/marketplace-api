package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class GetContributionsDetailsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    private BillingProfileStoragePort billingProfileStoragePort;

    @Test
    void should_return_404_when_not_found() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

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
    void should_return_pull_request_review_state() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTION_BY_ID,
                        "90fb751a-1137-4815-b3c4-54927a5db059",
                        "97735d1fae1597c261d55e07ac10c947ae99ed2d0daf2826938e2f2de9b69ac8")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.githubPullRequestReviewState").isEqualTo("APPROVED");
    }

    @Test
    void should_return_contribution_details_when_found() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var billingProfile = billingProfileStoragePort.findIndividualBillingProfileForUser(UserId.of(antho.user().getId())).orElseThrow();
        billingProfileStoragePort.updateBillingProfileStatus(billingProfile.getId(), VerificationStatus.VERIFIED);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTION_BY_ID,
                        "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                        "b66cd16a35e0043d86f1850eb9ba6519d20ff833394f7516b0842fa2f18a5abf")))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "type": "PULL_REQUEST",
                          "contributor": {
                            "githubUserId": 43467246,
                            "login": "AnthonyBuisset",
                            "htmlUrl": "https://github.com/AnthonyBuisset",
                            "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                            "isRegistered": true
                          },
                          "repo": {
                            "id": 498695724,
                            "owner": "onlydustxyz",
                            "name": "marketplace-frontend",
                            "description": null,
                            "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                          },
                          "githubAuthor": {
                            "githubUserId": 34384633,
                            "login": "tdelabro",
                            "htmlUrl": "https://github.com/tdelabro",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4"
                          },
                          "githubNumber": 62,
                          "githubStatus": "DRAFT",
                          "githubTitle": "Anthony buisset feature/starknet",
                          "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/62",
                          "githubBody": null,
                          "githubCodeReviewOutcome": null,
                          "id": "b66cd16a35e0043d86f1850eb9ba6519d20ff833394f7516b0842fa2f18a5abf",
                          "createdAt": "2022-07-08T13:07:29Z",
                          "completedAt": "2022-07-08T13:17:51Z",
                          "status": "COMPLETED",
                          "project": {
                            "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                            "slug": "kaaper",
                            "name": "kaaper",
                            "shortDescription": "Documentation generator for Cairo projects.",
                            "logoUrl": null,
                            "visibility": "PUBLIC"
                          },
                          "commentsCount": 0,
                          "commitsCount": 1,
                          "userCommitsCount": 1,
                          "links": [],
                          "githubPullRequestReviewState": "PENDING_REVIEWER",
                          "rewards": [
                            {
                              "id": "6587511b-3791-47c6-8430-8f793606c63a",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "createdAt": "2023-09-20T08:01:47.616674Z",
                              "processedAt": null
                            },
                            {
                              "id": "0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "createdAt": "2023-09-20T08:00:46.580407Z",
                              "processedAt": null
                            },
                            {
                              "id": "335e45a5-7f59-4519-8a12-1addc530214c",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "createdAt": "2023-09-20T08:00:18.005344Z",
                              "processedAt": null
                            },
                            {
                              "id": "e9ebbe59-fb74-4a6c-9a51-6d9050412977",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "createdAt": "2023-09-20T08:02:53.470663Z",
                              "processedAt": null
                            },
                            {
                              "id": "e33ea956-d2f5-496b-acf9-e2350faddb16",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "createdAt": "2023-09-20T08:01:16.850492Z",
                              "processedAt": null
                            },
                            {
                              "id": "dd7d445f-6915-4955-9bae-078173627b05",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "createdAt": "2023-09-20T07:59:47.012001Z",
                              "processedAt": null
                            },
                            {
                              "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "createdAt": "2023-09-20T07:59:16.657487Z",
                              "processedAt": null
                            },
                            {
                              "id": "95e079c9-609c-4531-8c5c-13217306b299",
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                              "amount": 1000,
                              "dollarsEquivalent": 1010,
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": null
                              },
                              "to": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
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
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

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
                          "type": "CODE_REVIEW",
                          "contributor": {
                            "githubUserId": 43467246,
                            "login": "AnthonyBuisset",
                            "htmlUrl": "https://github.com/AnthonyBuisset",
                            "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                            "isRegistered": true
                          },
                          "repo": {
                            "id": 493591124,
                            "owner": "onlydustxyz",
                            "name": "kaaper",
                            "description": null,
                            "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                          },
                          "githubAuthor": {
                            "githubUserId": 43467246,
                            "login": "AnthonyBuisset",
                            "htmlUrl": "https://github.com/AnthonyBuisset",
                            "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                          },
                          "githubNumber": 17,
                          "githubStatus": "APPROVED",
                          "githubTitle": "Feat/view",
                          "githubHtmlUrl": "https://github.com/onlydustxyz/kaaper/pull/17",
                          "githubBody": null,
                          "githubCodeReviewOutcome": null,
                          "id": "9e374e732c9017c3fee800d686e02962dd21b69d9a11c4c21517d76fec56b1a1",
                          "createdAt": "2022-07-12T09:26:34Z",
                          "completedAt": "2022-07-12T15:32:58Z",
                          "status": "COMPLETED",
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
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                              },
                              "githubAuthor": {
                                "githubUserId": 26416205,
                                "login": "internnos",
                                "htmlUrl": "https://github.com/internnos",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/26416205?v=4"
                              },
                              "githubNumber": 17,
                              "githubStatus": "DRAFT",
                              "githubTitle": "Feat/view",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/kaaper/pull/17",
                              "githubBody": null,
                              "githubCodeReviewOutcome": null,
                              "is_mine": false
                            }
                          ],
                          "githubPullRequestReviewState": null,
                          "rewards": []
                        }
                                                
                        """)
        ;
    }

    @Test
    void should_return_draft_status() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

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
                .jsonPath("$.githubStatus").isEqualTo("DRAFT")
        ;
    }


    @Test
    void should_return_403_when_not_mine() {
        // Given
        final String jwt = userAuthHelper.authenticateHayden().jwt();

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

    @Test
    void should_return_200_when_leader() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTION_BY_ID,
                        "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                        "2eba1ee6dd0e0a3acb2f0d411eea52500eb572b1eec6f2ccf45bc53e8cd77bd6")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }
}
