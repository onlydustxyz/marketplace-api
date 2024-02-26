package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.CurrencyContract;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardResponseDTO;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;
import static org.assertj.core.api.Assertions.assertThat;


public class ProjectPostRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    public ProjectRepository projectRepository;

    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;

    @Test
    public void should_be_unauthorized() {
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.ONE)
                .currency(CurrencyContract.USD)
                .recipientId(1L);
        final UUID projectId = UUID.randomUUID();

        // When
        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(401);
    }

    @Test
    void should_be_forbidden_given_authenticated_user_not_project_lead() {
        // Given
        userAuthHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = userAuthHelper.authenticateUser(1L).jwt();
        final UUID projectId = projectRepository.findAll().get(0).getId();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.ONE)
                .currency(CurrencyContract.USD)
                .items(List.of(
                        new RewardItemRequest().id("pr1")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(2L)
                ))
                .recipientId(1L);

        // When
        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403)
                .expectBody()
                .jsonPath("$.message").isEqualTo("User must be project lead to request a reward");
    }

    @Test
    void should_not_be_able_to_request_reward_when_there_is_not_enough_budget() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(1))
                .currency(CurrencyContract.STRK)
                .recipientId(11111L)
                .items(List.of(
                        new RewardItemRequest().id("pr2")
                                .type(RewardType.PULL_REQUEST)
                                .number(2L)
                                .repoId(3L)
                ));

        // When
        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(400);
    }

    @Test
    void should_request_reward_to_old_api_given_a_project_lead() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(12.95))
                .currency(CurrencyContract.ETH)
                .recipientId(pierre.user().getGithubUserId())
                .items(List.of(
                        new RewardItemRequest().id("pr1")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(2L),
                        new RewardItemRequest().id("issue1")
                                .type(RewardType.ISSUE)
                                .number(2L)
                                .repoId(3L),
                        new RewardItemRequest().id("codeReview1")
                                .type(RewardType.CODE_REVIEW)
                                .number(3L)
                                .repoId(4L)
                ));

        final var newRewardId = UUID.randomUUID();

        paymentRequestRepository.saveAndFlush(PaymentRequestEntity.builder()
                .id(newRewardId)
                .projectId(projectId)
                .requestorId(pierre.user().getId())
                .recipientId(pierre.user().getGithubUserId())
                .amount(BigDecimal.valueOf(12.95))
                .currency(CurrencyEnumEntity.eth)
                .requestedAt(new Date())
                .hoursWorked(0)
                .build());

        // When
        rustApiWireMockServer.stubFor(WireMock.post(
                        WireMock.urlEqualTo("/api/payments"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                          "recipientId": 16590657,
                          "requestorId": "fc92397c-3431-4a84-8054-845376b630a0",
                          "amount": 12.95,
                          "currency": "ETH",
                          "reason": {
                            "workItems": [
                              {
                                "id": "pr1",
                                "type": "PULL_REQUEST",
                                "repoId": 2,
                                "number": 1
                              },
                              {
                                "id": "issue1",
                                "type": "ISSUE",
                                "repoId": 3,
                                "number": 2
                              },
                              {
                                "id": "codeReview1",
                                "type": "CODE_REVIEW",
                                "repoId": 4,
                                "number": 3
                              }
                            ]
                          }
                        }""")
                ).willReturn(
                        ResponseDefinitionBuilder.okForJson(RequestRewardResponseDTO.builder()
                                .commandId(UUID.randomUUID())
                                .paymentId(newRewardId)
                                .build())
                ));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/16590657"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));


        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .jsonPath("$.id").isEqualTo(newRewardId.toString());

        assertThat(paymentRequestRepository.findById(newRewardId).orElseThrow().getUsdAmount()).isEqualByComparingTo(BigDecimal.valueOf(23076.641));
    }

    @Test
    void should_fail_to_request_reward_when_old_api_is_down() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(12.95))
                .currency(CurrencyContract.ETH)
                .recipientId(pierre.user().getGithubUserId())
                .items(List.of(
                        new RewardItemRequest().id("pr1")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(2L),
                        new RewardItemRequest().id("issue1")
                                .type(RewardType.ISSUE)
                                .number(2L)
                                .repoId(3L),
                        new RewardItemRequest().id("codeReview1")
                                .type(RewardType.CODE_REVIEW)
                                .number(3L)
                                .repoId(4L)
                ));

        // When
        rustApiWireMockServer.stubFor(WireMock.post(
                        WireMock.urlEqualTo("/api/payments"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                          "recipientId": 16590657,
                          "requestorId": "fc92397c-3431-4a84-8054-845376b630a0",
                          "amount": 12.95,
                          "currency": "ETH",
                          "reason": {
                            "workItems": [
                              {
                                "id": "pr1",
                                "type": "PULL_REQUEST",
                                "repoId": 2,
                                "number": 1
                              },
                              {
                                "id": "issue1",
                                "type": "ISSUE",
                                "repoId": 3,
                                "number": 2
                              },
                              {
                                "id": "codeReview1",
                                "type": "CODE_REVIEW",
                                "repoId": 4,
                                "number": 3
                              }
                            ]
                          }
                        }""")
                ).willReturn(
                        ResponseDefinitionBuilder.like(ResponseDefinitionBuilder.jsonResponse("""
                                {
                                  "error": "Internal Server Error",
                                  "message": "Internal Server Error",
                                  "status": 500
                                }""", 500))
                ));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/16590657"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));


        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    void should_fail_to_request_reward_when_indexer_is_down() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(12.95))
                .currency(CurrencyContract.ETH)
                .recipientId(pierre.user().getGithubUserId())
                .items(List.of(
                        new RewardItemRequest().id("pr1")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(2L),
                        new RewardItemRequest().id("issue1")
                                .type(RewardType.ISSUE)
                                .number(2L)
                                .repoId(3L),
                        new RewardItemRequest().id("codeReview1")
                                .type(RewardType.CODE_REVIEW)
                                .number(3L)
                                .repoId(4L)
                ));

        final var newRewardId = UUID.randomUUID();

        // When
        rustApiWireMockServer.stubFor(WireMock.post(
                        WireMock.urlEqualTo("/api/payments"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                          "recipientId": 16590657,
                          "requestorId": "fc92397c-3431-4a84-8054-845376b630a0",
                          "amount": 12.95,
                          "currency": "ETH",
                          "reason": {
                            "workItems": [
                              {
                                "id": "pr1",
                                "type": "PULL_REQUEST",
                                "repoId": 2,
                                "number": 1
                              },
                              {
                                "id": "issue1",
                                "type": "ISSUE",
                                "repoId": 3,
                                "number": 2
                              },
                              {
                                "id": "codeReview1",
                                "type": "CODE_REVIEW",
                                "repoId": 4,
                                "number": 3
                              }
                            ]
                          }
                        }""")
                ).willReturn(
                        ResponseDefinitionBuilder.okForJson(RequestRewardResponseDTO.builder()
                                .commandId(UUID.randomUUID())
                                .paymentId(newRewardId)
                                .build())
                ));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/16590657"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.like(ResponseDefinitionBuilder.jsonResponse("""
                        {
                          "error": "Internal Server Error",
                          "message": "Internal Server Error",
                          "status": 500
                        }""", 500)
                )));


        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    void should_request_reward_to_old_api_given_a_project_lead_impersonated() {
        // Given
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), 2L, faker.rickAndMorty().character(),
                faker.internet().url(), true).jwt();
        userAuthHelper.authenticateUser(2L);
        final String impersonatePierreHeader =
                userAuthHelper.getImpersonationHeaderToImpersonatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(111.47))
                .currency(CurrencyContract.USDC)
                .recipientId(11111L)
                .items(List.of(
                        new RewardItemRequest().id("pr2")
                                .type(RewardType.PULL_REQUEST)
                                .number(2L)
                                .repoId(3L),
                        new RewardItemRequest().id("issue2")
                                .type(RewardType.ISSUE)
                                .number(3L)
                                .repoId(4L),
                        new RewardItemRequest().id("codeReview2")
                                .type(RewardType.CODE_REVIEW)
                                .number(4L)
                                .repoId(5L)
                ));

        final var newRewardId = UUID.randomUUID();

        paymentRequestRepository.saveAndFlush(PaymentRequestEntity.builder()
                .id(newRewardId)
                .projectId(projectId)
                .requestorId(userAuthHelper.authenticatePierre().user().getId())
                .recipientId(11111L)
                .amount(BigDecimal.valueOf(111.47))
                .currency(CurrencyEnumEntity.usdc)
                .requestedAt(new Date())
                .hoursWorked(0)
                .build());

        // When
        rustApiWireMockServer.stubFor(WireMock.post(
                        WireMock.urlEqualTo("/api/payments"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                          "recipientId": 11111,
                          "requestorId": "fc92397c-3431-4a84-8054-845376b630a0",
                          "amount": 111.47,
                          "currency": "USDC",
                          "reason": {
                            "workItems": [
                              {
                                "id": "pr2",
                                "type": "PULL_REQUEST",
                                "repoId": 3,
                                "number": 2
                              },
                              {
                                "id": "issue2",
                                "type": "ISSUE",
                                "repoId": 4,
                                "number": 3
                              },
                              {
                                "id": "codeReview2",
                                "type": "CODE_REVIEW",
                                "repoId": 5,
                                "number": 4
                              }
                            ]
                          }
                        }""")
                ).willReturn(
                        ResponseDefinitionBuilder.okForJson(RequestRewardResponseDTO.builder()
                                .commandId(UUID.randomUUID())
                                .paymentId(newRewardId)
                                .build())
                ));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/11111"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));


        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .header(IMPERSONATION_HEADER, impersonatePierreHeader)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .jsonPath("$.id").isEqualTo(newRewardId.toString());

        assertThat(paymentRequestRepository.findById(newRewardId).orElseThrow().getUsdAmount()).isEqualTo(BigDecimal.valueOf(112.5847));
    }


}
