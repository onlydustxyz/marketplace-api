package onlydust.com.marketplace.api.bootstrap.it.api;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutPreferenceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayoutPreferenceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class MeGetRewardApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;
    @Autowired
    BillingProfileRepository billingProfileRepository;
    @Autowired
    PayoutPreferenceRepository payoutPreferenceRepository;
    @Autowired
    RewardRepository rewardRepository;

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
    void should_get_user_reward() throws ParseException {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticatePierre();
        final String jwt = authenticatedUser.jwt();
        final UUID rewardId = UUID.fromString("2ac80cc6-7e83-4eef-bc0c-932b58f683c0");

        final var reward = rewardRepository.findById(rewardId).orElseThrow();
        final var billingProfileId =
                payoutPreferenceRepository.findById(PayoutPreferenceEntity.PrimaryKey.builder().projectId(reward.getProjectId()).userId(authenticatedUser.user().getId()).build()).orElseThrow().getBillingProfileId();
        final var billingProfile = billingProfileRepository.findById(billingProfileId).orElseThrow();
        billingProfile.setVerificationStatus(VerificationStatusEntity.VERIFIED);
        billingProfileRepository.save(billingProfile);

        // When
        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0",
                          "currency": "USDC",
                          "amount": 1000,
                          "dollarsEquivalent": 1010.00,
                          "status": "PENDING_INVOICE",
                          "unlockDate": null,
                          "from": {
                            "githubUserId": 16590657,
                            "login": "PierreOucif",
                            "htmlUrl": null,
                            "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                            "isRegistered": null
                          },
                          "to": {
                            "githubUserId": 16590657,
                            "login": "PierreOucif",
                            "htmlUrl": null,
                            "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                            "isRegistered": null
                          },
                          "createdAt": "2023-09-19T07:38:22.018458Z",
                          "processedAt": null,
                          "project": {
                            "id": "f39b827f-df73-498c-8853-99bc3f562723",
                            "slug": "qa-new-contributions",
                            "name": "QA new contributions",
                            "shortDescription": "QA new contributions",
                            "logoUrl": null,
                            "visibility": "PUBLIC"
                          },
                          "receipt": null
                        }
                        """);

        final PaymentRequestEntity paymentRequestEntity = paymentRequestRepository.findById(rewardId).orElseThrow();
        paymentRequestEntity.setAmount(BigDecimal.valueOf(100));
        paymentRequestEntity.setCurrency(CurrencyEnumEntity.strk);
        paymentRequestEntity.setUsdAmount(null);
        paymentRequestRepository.save(paymentRequestEntity);
        final UUID paymentId = UUID.randomUUID();
        final Date processedAt = new SimpleDateFormat("yyyy-MM-dd").parse("2023-09-20");
        paymentRepository.save(PaymentEntity.builder()
                .id(paymentId)
                .amount(paymentRequestEntity.getAmount())
                .requestId(paymentRequestEntity.getId())
                .processedAt(processedAt)
                .currencyCode(paymentRequestEntity.getCurrency().name())
                .receipt(JacksonUtil.toJsonNode("""
                        {"Sepa": {"recipient_iban": "FR7640618802650004034616521", "transaction_reference": "IBAN OK"}}"""))
                .build());

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(String.format("""
                        {
                           "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0",
                           "currency": "STRK",
                           "amount": 100,
                           "dollarsEquivalent": null,
                           "status": "COMPLETE",
                           "from": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "to": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "createdAt": "2023-09-19T07:38:22.018458Z",
                           "processedAt": "%s",
                           "receipt": {
                               "type": "FIAT",
                               "iban": "FR7640618802650004034616521",
                               "walletAddress": null,
                               "ens": null,
                               "transactionReference": "IBAN OK"
                             }
                         }
                         """, DateMapper.toZoneDateTime(processedAt).format(DateTimeFormatter.ISO_INSTANT)));

        final PaymentEntity paymentEntity = paymentRepository.findById(paymentId).orElseThrow();
        paymentEntity.setReceipt(JacksonUtil.toJsonNode("""
                {"Ethereum": {"recipient_ens": "ilysse.eth", "transaction_hash": "0x0000000000000000000000000000000000000000000000000000000000000000", "recipient_address": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea"}}"""));
        paymentRepository.save(paymentEntity);

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(String.format("""
                        {
                           "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0",
                           "currency": "STRK",
                           "amount": 100,
                           "dollarsEquivalent": null,
                           "status": "COMPLETE",
                           "from": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "to": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "createdAt": "2023-09-19T07:38:22.018458Z",
                           "processedAt": "%s",
                           "receipt": {
                               "type": "CRYPTO",
                               "iban": null,
                               "walletAddress": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ea",
                               "ens": "ilysse.eth",
                               "transactionReference": "0x0000000000000000000000000000000000000000000000000000000000000000",
                               "transactionReferenceLink": "https://etherscan.io/tx/0x0000000000000000000000000000000000000000000000000000000000000000"
                             }
                         }
                         """, DateMapper.toZoneDateTime(processedAt).format(DateTimeFormatter.ISO_INSTANT)));

        final PaymentEntity paymentEntity2 = paymentRepository.findById(paymentId).orElseThrow();
        paymentEntity2.setReceipt(JacksonUtil.toJsonNode("""
                {"Aptos": {"recipient_ens": "ilysse.eth", "transaction_hash": "0x0000000000000000000000000000000000000000000000000000000000000001", "recipient_address": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283eb"}}"""));
        paymentRepository.save(paymentEntity2);

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(String.format("""
                        {
                           "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0",
                           "currency": "STRK",
                           "amount": 100,
                           "dollarsEquivalent": null,
                           "status": "COMPLETE",
                           "from": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "to": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "createdAt": "2023-09-19T07:38:22.018458Z",
                           "processedAt": "%s",
                           "receipt": {
                               "type": "CRYPTO",
                               "iban": null,
                               "walletAddress": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283eb",
                               "ens": "ilysse.eth",
                               "transactionReference": "0x0000000000000000000000000000000000000000000000000000000000000001",
                               "transactionReferenceLink": "https://aptoscan.com/version/0x0000000000000000000000000000000000000000000000000000000000000001"
                             }
                         }
                         """, DateMapper.toZoneDateTime(processedAt).format(DateTimeFormatter.ISO_INSTANT)));
        final PaymentEntity paymentEntity3 = paymentRepository.findById(paymentId).orElseThrow();
        paymentEntity3.setReceipt(JacksonUtil.toJsonNode("""
                {"Optimism": {"recipient_ens": "ilysse.eth", "transaction_hash": "0x0000000000000000000000000000000000000000000000000000000000000002", "recipient_address": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ec"}}"""));
        paymentRepository.save(paymentEntity3);

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(String.format("""
                        {
                           "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0",
                           "currency": "STRK",
                           "amount": 100,
                           "dollarsEquivalent": null,
                           "status": "COMPLETE",
                           "from": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "to": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "createdAt": "2023-09-19T07:38:22.018458Z",
                           "processedAt": "%s",
                           "receipt": {
                               "type": "CRYPTO",
                               "iban": null,
                               "walletAddress": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ec",
                               "ens": "ilysse.eth",
                               "transactionReference": "0x0000000000000000000000000000000000000000000000000000000000000002",
                               "transactionReferenceLink": "https://optimistic.etherscan.io/tx/0x0000000000000000000000000000000000000000000000000000000000000002"
                             }
                         }
                         """, DateMapper.toZoneDateTime(processedAt).format(DateTimeFormatter.ISO_INSTANT)));

        final PaymentEntity paymentEntity4 = paymentRepository.findById(paymentId).orElseThrow();
        paymentEntity4.setReceipt(JacksonUtil.toJsonNode("""
                {"Starknet": {"recipient_ens": "ilysse.eth", "transaction_hash": "0x0000000000000000000000000000000000000000000000000000000000000003", "recipient_address": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ed"}}"""));
        paymentRepository.save(paymentEntity4);

        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(String.format("""
                        {
                           "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0",
                           "currency": "STRK",
                           "amount": 100,
                           "dollarsEquivalent": null,
                           "status": "COMPLETE",
                           "from": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "to": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "isRegistered": null
                           },
                           "createdAt": "2023-09-19T07:38:22.018458Z",
                           "processedAt": "%s",
                           "receipt": {
                               "type": "CRYPTO",
                               "iban": null,
                               "walletAddress": "0x657dd41d9bbfe65cbe9f6224d48405b7cad283ed",
                               "ens": "ilysse.eth",
                               "transactionReference": "0x0000000000000000000000000000000000000000000000000000000000000003",
                               "transactionReferenceLink": "https://starkscan.co/tx/0x0000000000000000000000000000000000000000000000000000000000000003"
                             }
                         }
                         """, DateMapper.toZoneDateTime(processedAt).format(DateTimeFormatter.ISO_INSTANT)));
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
