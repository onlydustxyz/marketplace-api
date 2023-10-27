package onlydust.com.marketplace.api.bootstrap.it;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.CurrencyContract;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.dto.RequestRewardResponseDTO;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;

@ActiveProfiles({"hasura_auth"})
public class ProjectPostRewardsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    public HasuraUserHelper hasuraUserHelper;
    @Autowired
    public ProjectRepository projectRepository;

    @Autowired
    AuthenticationService authenticationService;

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
        hasuraUserHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = hasuraUserHelper.authenticateUser(1L).jwt();
        final UUID projectId = projectRepository.findAll().get(0).getId();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.ONE)
                .currency(CurrencyContract.USD)
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
                .jsonPath("$.message").isEqualTo("FORBIDDEN");
    }

    @Test
    void should_not_be_able_to_request_reward_when_there_is_not_enough_budget() {
        // Given
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(1))
                .currency(CurrencyContract.STARK)
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
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
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
                .withHeader("Authorization", equalTo(BEARER_PREFIX + jwt))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                          "recipientId": 16590657,
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
    }

    @Test
    void should_request_reward_to_old_api_given_a_project_lead_impersonated() {
        // Given
        final String jwt = hasuraUserHelper.newFakeUser(UUID.randomUUID(), 2L, faker.rickAndMorty().character(),
                faker.internet().url(), true).jwt();
        hasuraUserHelper.authenticateUser(2L);
        final String impersonatePierreHeader =
                hasuraUserHelper.getImpersonationHeaderToImpersonatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(111.47))
                .currency(CurrencyContract.USD)
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

        // When
        rustApiWireMockServer.stubFor(WireMock.post(
                        WireMock.urlEqualTo("/api/payments"))
                .withHeader("Authorization", equalTo(BEARER_PREFIX + jwt))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withHeader(IMPERSONATION_HEADER, equalTo(impersonatePierreHeader))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                          "recipientId": 11111,
                          "amount": 111.47,
                          "currency": "USD",
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
    }


}
