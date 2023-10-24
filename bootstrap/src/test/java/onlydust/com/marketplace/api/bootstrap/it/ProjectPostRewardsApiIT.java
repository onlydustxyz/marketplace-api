package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.CurrencyContract;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class ProjectPostRewardsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    public HasuraUserHelper hasuraUserHelper;
    @Autowired
    public ProjectRepository projectRepository;

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
    void should_be_forbidden_given_authenticated_user_not_project_lead() throws JsonProcessingException {
        // Given
        hasuraUserHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url());
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
    void should_request_reward_to_old_api_given_a_project_lead() throws JsonProcessingException {
        // Given
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(212.95))
                .currency(CurrencyContract.STARK)
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
                .withHeader("Authorization", WireMock.equalTo(BEARER_PREFIX + jwt))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString("""
                        {
                        "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                        "recipientId": 16590657,
                        "amount": 212.95,
                        "currency": "USD",
                        "reason": {
                            "workItems": [
                                {
                                "
                                }
                            ]
                        }
                        }
                        """))
                ));


        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(200);
    }
}
