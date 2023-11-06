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
                .uri(getApiURI(String.format(CONTRIBUTIONS_GET_BY_ID, "000000")))
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
                .uri(getApiURI(String.format(CONTRIBUTIONS_GET_BY_ID,
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
                          "githubNumber": 62,
                          "githubTitle": "Anthony buisset feature/starknet",
                          "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/62",
                          "githubBody": null,
                          "project_name": "Mooooooonlight",
                          "repo_name": "marketplace-frontend",
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
}
