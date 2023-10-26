package onlydust.com.marketplace.api.bootstrap.it;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CryptoUsdQuotesEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class ProjectsGetRewardApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper userHelper;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;

    @Test
    void should_return_a_403_given_not_project_lead_to_get_reward() {
        // Given
        final String jwt = userHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().location(),
                faker.internet().url(), false).jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    void should_get_reward_given_a_project_lead() throws ParseException {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "id": "85f8358c-5339-42ac-a577-16d7760d1e28",
                            "currency": "USD",
                            "amount": 1000,
                            "dollarsEquivalent": 1000,
                            "status": "PROCESSING",
                            "from": {
                                "id": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": null
                            },
                            "to": {
                                "id": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": null
                            },
                            "createdAt": "2023-09-19T05:38:52.590518Z",
                            "processedAt": null
                        }""");


        final PaymentRequestEntity paymentRequestEntity = paymentRequestRepository.findById(rewardId).orElseThrow();
        paymentRequestEntity.setAmount(BigDecimal.valueOf(100));
        paymentRequestEntity.setCurrency(CurrencyEnumEntity.stark);
        paymentRequestRepository.save(paymentRequestEntity);
        paymentRepository.save(PaymentEntity.builder()
                .id(UUID.randomUUID())
                .amount(paymentRequestEntity.getAmount())
                .requestId(paymentRequestEntity.getId())
                .processedAt(new SimpleDateFormat("yyyy-MM-dd").parse("2023-09-20"))
                .currencyCode(paymentRequestEntity.getCurrency().name())
                .receipt(JacksonUtil.toJsonNode("{}"))
                .build());

        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "id": "85f8358c-5339-42ac-a577-16d7760d1e28",
                            "currency": "STARK",
                            "amount": 100,
                            "dollarsEquivalent": null,
                            "status": "COMPLETE",
                            "from": {
                                "id": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": null
                            },
                            "to": {
                                "id": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": null
                            },
                            "createdAt": "2023-09-19T05:38:52.590518Z",
                            "processedAt": "2023-09-19T22:00:00Z"
                        }""");

        paymentRequestEntity.setAmount(BigDecimal.valueOf(200));
        paymentRequestEntity.setCurrency(CurrencyEnumEntity.eth);
        paymentRequestRepository.save(paymentRequestEntity);
        cryptoUsdQuotesRepository.save(new CryptoUsdQuotesEntity(CurrencyEnumEntity.eth, BigDecimal.valueOf(1547.66),
                new Date()));

        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "id": "85f8358c-5339-42ac-a577-16d7760d1e28",
                            "currency": "ETH",
                            "amount": 200,
                            "dollarsEquivalent": 309532,
                            "status": "COMPLETE",
                            "from": {
                                "id": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": null
                            },
                            "to": {
                                "id": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": null
                            },
                            "createdAt": "2023-09-19T05:38:52.590518Z",
                            "processedAt": "2023-09-19T22:00:00Z"
                        }""");
    }


    @Test
    void should_return_a_403_given_not_project_lead_to_get_reward_items() {
        // Given
        final String jwt = userHelper.newFakeUser(UUID.randomUUID(), 2L, faker.rickAndMorty().location(),
                faker.internet().url(), false).jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD_ITEMS, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    void should_return_pagination_reward_items_given_a_project_lead() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD_ITEMS, projectId, rewardId), Map.of("pageSize", "2",
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
                                    "number": 1,
                                    "id": "1511546916",
                                    "title": "Addin sitemap.xml in robots.txt",
                                    "githubUrl": "https://github.com/onlydustxyz/marketplace/pull/1232",
                                    "createdAt": "2023-09-12T05:38:04Z",
                                    "lastUpdateAt": "2023-09-12T05:45:12Z",
                                    "repoName": "marketplace-frontend",
                                    "type": "PULL_REQUEST",
                                    "commitsCount": 1,
                                    "userCommitsCount": 1,
                                    "commentsCount": null,
                                    "status": "COMPLETED",
                                    "githubAuthorId": 16590657,
                                    "authorLogin": "PierreOucif",
                                    "authorAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                    "authorGithubUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                },
                                {
                                    "number": 3,
                                    "id": "1507455279",
                                    "title": "E 730 migrate oscar frontend documentation",
                                    "githubUrl": "https://github.com/onlydustxyz/marketplace/pull/1225",
                                    "createdAt": "2023-09-08T06:14:32Z",
                                    "lastUpdateAt": "2023-09-08T06:19:55Z",
                                    "repoName": "marketplace-frontend",
                                    "type": "PULL_REQUEST",
                                    "commitsCount": 3,
                                    "userCommitsCount": 3,
                                    "commentsCount": null,
                                    "status": "COMPLETED",
                                    "githubAuthorId": 16590657,
                                    "authorLogin": "PierreOucif",
                                    "authorAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                    "authorGithubUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                }
                            ],
                            "hasMore": true,
                            "totalPageNumber": 13,
                            "totalItemNumber": 26,
                            "nextPageIndex": 1
                        }""");
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD_ITEMS, projectId, rewardId), Map.of("pageSize", "2",
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
                                    "number": 1,
                                    "id": "1449825806",
                                    "title": "Adding gitguardian pre-commit",
                                    "githubUrl": "https://github.com/onlydustxyz/marketplace/pull/1138",
                                    "createdAt": "2023-07-26T07:04:28Z",
                                    "lastUpdateAt": "2023-07-26T09:38:35Z",
                                    "repoName": "marketplace-frontend",
                                    "type": "PULL_REQUEST",
                                    "commitsCount": 1,
                                    "userCommitsCount": 1,
                                    "commentsCount": null,
                                    "status": "COMPLETED",
                                    "githubAuthorId": 16590657,
                                    "authorLogin": "PierreOucif",
                                    "authorAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                    "authorGithubUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                },
                                {
                                    "number": 16,
                                    "id": "1442413635",
                                    "title": "First API integration test",
                                    "githubUrl": "https://github.com/onlydustxyz/marketplace/pull/1129",
                                    "createdAt": "2023-07-20T06:45:18Z",
                                    "lastUpdateAt": "2023-07-21T11:00:05Z",
                                    "repoName": "marketplace-frontend",
                                    "type": "PULL_REQUEST",
                                    "commitsCount": 39,
                                    "userCommitsCount": 16,
                                    "commentsCount": null,
                                    "status": "COMPLETED",
                                    "githubAuthorId": 43467246,
                                    "authorLogin": "AnthonyBuisset",
                                    "authorAvatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                    "authorGithubUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                }
                            ],
                            "hasMore": false,
                            "totalPageNumber": 13,
                            "totalItemNumber": 26,
                            "nextPageIndex": 12
                        }""");
    }

    ;


}
