package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ReceiptEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class MeGetRewardApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    RewardRepository rewardRepository;
    @Autowired
    RewardStatusRepository rewardStatusRepository;
    @Autowired
    CurrencyRepository currencyRepository;

    @Test
    void should_return_a_403_given_user_not_linked_to_reward() {
        // Given
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().location(),
                faker.internet().url(), false).jwt();
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    void should_return_a_403_given_user_not_linked_to_reward_to_get_reward_items() {
        // Given
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), 5L, faker.rickAndMorty().location(),
                faker.internet().url(), false).jwt();
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(ME_REWARD_ITEMS, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    void should_return_pagination_reward_items() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID rewardId = UUID.fromString("2ac80cc6-7e83-4eef-bc0c-932b58f683c0");

        // When
        client.get()
                .uri(getApiURI(String.format(ME_REWARD_ITEMS, rewardId), Map.of("pageSize", "2",
                        "pageIndex", "0")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewardItems": [
                            {
                              "number": 1232,
                              "id": "1511546916",
                              "contributionId": "a290ea203b1264105bf581aebbdf3e79edfdd89811da50dc6bd076272d810b2e",
                              "title": "Addin sitemap.xml in robots.txt",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1232",
                              "createdAt": "2023-09-12T07:38:04Z",
                              "completedAt": "2023-09-12T07:45:12Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": null,
                              "status": "MERGED",
                              "githubAuthorId": 16590657,
                              "authorLogin": "PierreOucif",
                              "authorAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "authorGithubUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "githubBody": null
                            },
                            {
                              "number": 1225,
                              "id": "1507455279",
                              "contributionId": "955b084215e36980ded785f5afef2725e82f24188a48afaa59d1909c97d60ad6",
                              "title": "E 730 migrate oscar frontend documentation",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1225",
                              "createdAt": "2023-09-08T08:14:32Z",
                              "completedAt": "2023-09-08T08:19:55Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 3,
                              "userCommitsCount": 3,
                              "commentsCount": null,
                              "status": "MERGED",
                              "githubAuthorId": 16590657,
                              "authorLogin": "PierreOucif",
                              "authorAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "authorGithubUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "githubBody": null
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 13,
                          "totalItemNumber": 25,
                          "nextPageIndex": 1
                        }
                        """);

        client.get()
                .uri(getApiURI(String.format(ME_REWARD_ITEMS, rewardId), Map.of("pageSize", "2",
                        "pageIndex", "12")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewardItems": [
                            {
                              "number": 1129,
                              "id": "1442413635",
                              "contributionId": "6d709dd5f85a8b8eaff9cc8837ab837ef9a1a1109ead76580490c0a730a87d9d",
                              "title": "First API integration test",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                              "createdAt": "2023-07-20T08:45:18Z",
                              "completedAt": "2023-07-21T13:00:05Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 30,
                              "userCommitsCount": 16,
                              "commentsCount": null,
                              "status": "MERGED",
                              "githubAuthorId": 43467246,
                              "authorLogin": "AnthonyBuisset",
                              "authorAvatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                              "authorGithubUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                              "githubBody": "IT test structure\\nDocker container support\\nFirst API integration test: create project"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 13,
                          "totalItemNumber": 25,
                          "nextPageIndex": 12
                        }
                        """);
    }


}
