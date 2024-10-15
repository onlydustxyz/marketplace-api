package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.suites.tags.TagReward;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagReward
public class ProjectPostRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    public ProjectRepository projectRepository;

    @Autowired
    AuthenticatedAppUserService authenticatedAppUserService;

    @Test
    public void should_be_unauthorized() {
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.ONE)
                .currencyId(CurrencyHelper.USD.value())
                .recipientId(1L);
        final var projectId = ProjectId.random();

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
        userAuthHelper.signUpUser(1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = userAuthHelper.authenticateUser(1L).jwt();
        final var projectId = projectRepository.findAll().get(0).getId();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.ONE)
                .currencyId(CurrencyHelper.USD.value())
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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(1))
                .currencyId(CurrencyHelper.STRK.value())
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
    void should_fail_to_request_reward_when_indexer_is_down() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(12.95))
                .currencyId(CurrencyHelper.ETH.value())
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

    @Autowired
    AccountingService accountingService;
    @Autowired
    CustomerIOProperties customerIOProperties;
    @Autowired
    PosthogProperties posthogProperties;

    @Test
    void should_create_reward() {
        // Given
        final UUID strkId = currencyRepository.findByCode("ETH").orElseThrow().id();
        final var sponsorId = SponsorId.of(UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d"));
        final SponsorAccountStatement strkSponsorAccount = accountingService.createSponsorAccountWithInitialBalance(
                sponsorId,
                Currency.Id.of(strkId), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final var programId = programHelper.randomId();
        accountingService.allocate(sponsorId, programId, PositiveAmount.of(100000L), Currency.Id.of(strkId));
        accountingService.grant(programId, ProjectId.of(projectId), PositiveAmount.of(100000L), Currency.Id.of(strkId));
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(12.95))
                .currencyId(CurrencyHelper.ETH.value())
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

        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getNewRewardReceivedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(pierre.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Reward received")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo("Good news! You just received a new reward for your " +
                                                                                                "contribution on <b>QA new contributions</b>:")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("See details")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link", equalTo("https://develop-app.onlydust.com/rewards")))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(pierre.user().getGithubLogin())))

                        .withRequestBody(matchingJsonPath("$.message_data.reward.currency", equalTo("ETH")))
                        .withRequestBody(matchingJsonPath("$.message_data.reward.amount", equalTo("12.950")))
                        .withRequestBody(matchingJsonPath("$.message_data.reward.sentBy", equalTo(pierre.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.reward.contributionsNumber", equalTo("0")))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(pierre.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("New reward received ✨")))
        );

        trackingOutboxJob.run();

        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("reward_received")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(pierre.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent())))
                .withRequestBody(matchingJsonPath("$.properties['amount']", equalTo("12.95")))
                .withRequestBody(matchingJsonPath("$.properties['project_id']", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['sender_id']", equalTo(pierre.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['currency']", equalTo("ETH"))));


        // Given
        final List<RewardRequest> rewardRequests = List.of(new RewardRequest()
                        .amount(BigDecimal.valueOf(150))
                        .currencyId(CurrencyHelper.ETH.value())
                        .recipientId(pierre.user().getGithubUserId())
                        .items(List.of(
                                new RewardItemRequest().id("issue1")
                                        .type(RewardType.ISSUE)
                                        .number(5L)
                                        .repoId(6L)
                        ))
                ,
                new RewardRequest()
                        .amount(BigDecimal.valueOf(200))
                        .currencyId(CurrencyHelper.ETH.value())
                        .recipientId(pierre.user().getGithubUserId())
                        .items(List.of(
                                new RewardItemRequest().id("pr1")
                                        .type(RewardType.PULL_REQUEST)
                                        .number(7L)
                                        .repoId(8L)
                        ))
        );

        // When
        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS_V2, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequests))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("sort", "REQUESTED_AT", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.rewards[0].amount.amount").isEqualTo(BigDecimal.valueOf(200))
                .jsonPath("$.rewards[1].amount.amount").isEqualTo(BigDecimal.valueOf(150));

    }

}
