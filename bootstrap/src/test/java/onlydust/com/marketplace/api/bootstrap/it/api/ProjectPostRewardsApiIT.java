package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.api.bootstrap.helper.CurrencyHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


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
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
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
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
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

    @Test
    void should_create_reward() {
        // Given
        final UUID strkId = currencyRepository.findByCode("ETH").orElseThrow().id();
        final SponsorAccountStatement strkSponsorAccount = accountingService.createSponsorAccountWithInitialBalance(
                SponsorId.of(UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d")),
                Currency.Id.of(strkId), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        accountingService.allocate(strkSponsorAccount.account().id(), ProjectId.of(projectId), PositiveAmount.of(100000L), Currency.Id.of(strkId));
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

        accountingMailOutboxJob.run();
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getNewRewardReceivedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(pierre.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.projectName", equalTo("QA new contributions")))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(pierre.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.currency", equalTo("ETH")))
                        .withRequestBody(matchingJsonPath("$.message_data.amount", equalTo("12.950")))
                        .withRequestBody(matchingJsonPath("$.message_data.sentBy", equalTo(pierre.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(pierre.user().getGithubEmail())))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustAdminEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("New reward received ✨")))
        );
    }
}
