package onlydust.com.marketplace.api.it.api.feature;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.node.guardians.NodeGuardiansApiProperties;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BoostNodeGuardiansRewardsEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NodeGuardianBoostRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BoostNodeGuardiansRewardsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.NodeGuardianBoostRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import onlydust.com.marketplace.api.read.entities.reward.RewardDetailsReadEntity;
import onlydust.com.marketplace.api.read.repositories.RewardDetailsReadRepository;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.port.input.BoostNodeGuardiansRewardsPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectRewardFacadePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

public class NodeGuardiansBoostRewardsIT extends AbstractMarketplaceApiIT {

    @Autowired
    BoostNodeGuardiansRewardsPort boostNodeGuardiansRewardsPort;
    @Autowired
    NodeGuardiansApiProperties nodeGuardiansApiProperties;
    @Autowired
    NodeGuardianBoostRewardRepository nodeGuardianBoostRewardRepository;
    @Autowired
    BoostNodeGuardiansRewardsRepository boostNodeGuardiansRewardsRepository;
    @Autowired
    ProjectRewardFacadePort rewardFacadePort;
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    @Autowired
    AccountingService accountingService;
    @Autowired
    GithubHttpClient.Config githubDustyBotConfig;
    @Autowired
    OutboxConsumerJob nodeGuardiansOutboxJob;

    public void setupNodeGuardiansApiMocks() {
        nodeGuardiansWireMockServer.stubFor(WireMock.get("/api/partnership/only-dust/ofux").withHeader("Authorization",
                WireMock.equalTo("Bearer %s".formatted(nodeGuardiansApiProperties.getApiKey()))).willReturn(okJson("""
                {
                  "level": 2
                }
                """)));
    }

    public void setupGithubApiMocks() {
        dustyBotApiWireMockServer.stubFor(post(urlEqualTo("/repos/onlydustxyz/marketplace-frontend/issues"))
                .withHeader("Authorization", equalTo("Bearer " + githubDustyBotConfig.getPersonalAccessToken()))
                .willReturn(okJson(CREATE_ISSUE_RESPONSE_JSON)));
        dustyBotApiWireMockServer.stubFor(post(urlEqualTo("/repos/onlydustxyz/marketplace-frontend/issues/25"))
                .withHeader("Authorization", equalTo("Bearer " + githubDustyBotConfig.getPersonalAccessToken()))
                .withRequestBody(equalToJson("""
                        {
                            "state": "closed"
                        }
                        """))
                .willReturn(okJson(String.format(CLOSE_ISSUE_RESPONSE_JSON, faker.rickAndMorty().character()))));
        dustyBotApiWireMockServer.stubFor(put(urlEqualTo("/api/v1/users/595505"))
                .withHeader("Authorization", equalTo("Bearer " + githubDustyBotConfig.getPersonalAccessToken()))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
    }

    public void addBudgetToProject(final ProjectId projectId) {
        final var sponsorId = sponsorHelper.create().id();
        final var programId = programHelper.create(sponsorId).id();
        accountingService.createSponsorAccountWithInitialBalance(sponsorId,
                Currency.Id.of(CurrencyHelper.STRK.value()), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb(), null));
        accountingService.createSponsorAccountWithInitialBalance(sponsorId,
                Currency.Id.of(CurrencyHelper.ETH.value()), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb(), null));
        accountingService.createSponsorAccountWithInitialBalance(sponsorId,
                Currency.Id.of(CurrencyHelper.USD.value()), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.SEPA, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb(), null));

        accountingService.allocate(sponsorId, programId, PositiveAmount.of(100000L), Currency.Id.of(CurrencyHelper.STRK.value()));
        accountingService.allocate(sponsorId, programId, PositiveAmount.of(100000L), Currency.Id.of(CurrencyHelper.ETH.value()));
        accountingService.allocate(sponsorId, programId, PositiveAmount.of(100000L), Currency.Id.of(CurrencyHelper.USD.value()));

        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(CurrencyHelper.STRK.value()));
        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(CurrencyHelper.ETH.value()));
        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(CurrencyHelper.USD.value()));
    }

    @Autowired
    ProjectRepoRepository projectRepoRepository;
    @Autowired
    RewardDetailsReadRepository rewardDetailsReadRepository;

    @Test
    void should_boost_rewards() {
        // Given
        setupNodeGuardiansApiMocks();
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticatePierre();
        final var marketplace = ProjectId.of("45ca43d6-130e-4bf7-9776-2b1eb1dcb782"); // Marketplace
        final var bretzel = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56"); // Bretzel
        addBudgetToProject(bretzel);
        addBudgetToProject(marketplace);
        final Long githubRepoId = 498695724L;
        final UUID ecosystemId = UUID.fromString("6ab7fa6c-c418-4997-9c5f-55fb021a8e5c"); // Ethereum linked to Bretzel
        projectLeadRepository.save(new ProjectLeadEntity(marketplace.value(), authenticatedUser.user().getId()));
        projectLeadRepository.save(new ProjectLeadEntity(bretzel.value(), authenticatedUser.user().getId()));
        final Long oliver = 595505L;
        projectRepoRepository.save(new ProjectRepoEntity(marketplace.value(), githubRepoId));

        // When
        boostNodeGuardiansRewardsPort.boostProject(marketplace, authenticatedUser.userId(), githubRepoId, ecosystemId);

        // Then
        assertEquals(0, nodeGuardianBoostRewardRepository.count());
        assertEquals(0, boostNodeGuardiansRewardsRepository.count());

        final var rewardId1 = sendRewardToRecipient(authenticatedUser.jwt(), oliver, 1000L, CurrencyHelper.STRK.value(), bretzel);
        final var rewardId2 = sendRewardToRecipient(authenticatedUser.jwt(), oliver, 500L, CurrencyHelper.ETH.value(), bretzel);
        final var rewardId3 = sendRewardToRecipient(authenticatedUser.jwt(), oliver, 200L, CurrencyHelper.USD.value(), bretzel);

        // When
        setupGithubApiMocks();
        boostNodeGuardiansRewardsPort.boostProject(marketplace, authenticatedUser.userId(), githubRepoId, ecosystemId);

        assertEquals(1, nodeGuardianBoostRewardRepository.count());
        assertEquals(1, boostNodeGuardiansRewardsRepository.count());
        for (NodeGuardianBoostRewardEntity nodeGuardianBoostRewardEntity : nodeGuardianBoostRewardRepository.findAll()) {
            assertTrue(List.of(rewardId1).contains(nodeGuardianBoostRewardEntity.getBoostedRewardId()));
            assertNull(nodeGuardianBoostRewardEntity.getBoostRewardId());
        }
        for (BoostNodeGuardiansRewardsEventEntity boostNodeGuardiansRewardsEventEntity : boostNodeGuardiansRewardsRepository.findAll()) {
            assertEquals(EventEntity.Status.PENDING, boostNodeGuardiansRewardsEventEntity.getStatus());
        }

        // When
        nodeGuardiansOutboxJob.run();
        assertEquals(1, nodeGuardianBoostRewardRepository.count());
        assertEquals(1, boostNodeGuardiansRewardsRepository.count());
        UUID boostRewardId1 = null;
        for (NodeGuardianBoostRewardEntity nodeGuardianBoostRewardEntity : nodeGuardianBoostRewardRepository.findAll()) {
            assertTrue(List.of(rewardId1).contains(nodeGuardianBoostRewardEntity.getBoostedRewardId()));
            assertNotNull(nodeGuardianBoostRewardEntity.getBoostRewardId());
            if (nodeGuardianBoostRewardEntity.getBoostedRewardId().equals(rewardId1)) {
                boostRewardId1 = nodeGuardianBoostRewardEntity.getBoostRewardId();
            }
        }
        for (BoostNodeGuardiansRewardsEventEntity boostNodeGuardiansRewardsEventEntity : boostNodeGuardiansRewardsRepository.findAll()) {
            assertEquals(EventEntity.Status.PROCESSED, boostNodeGuardiansRewardsEventEntity.getStatus());
        }

        final Page<RewardDetailsReadEntity> projectRewards = rewardDetailsReadRepository.findProjectRewards(marketplace.value(), null, List.of(oliver), null,
                null,
                null,
                PageRequest.of(0, 50, RewardDetailsReadRepository.sortBy(RewardsSort.REQUESTED_AT,
                        SortDirection.DESC)));

        final UUID finalBoostRewardId1 = boostRewardId1;
        final RewardDetailsReadEntity rewardBoost1 = projectRewards.getContent().stream()
                .filter(readEntity -> readEntity.getId().equals(finalBoostRewardId1)).findFirst().orElseThrow();
        assertEquals(rewardBoost1.amount().getAmount().doubleValue(), 50.0D);
        assertEquals(rewardBoost1.amount().getCurrency().getCode(), "STRK");

    }

    private UUID sendRewardToRecipient(String jwt, Long recipientId, Long amount, UUID currencyId, ProjectId projectId) {
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(amount))
                .currencyId(currencyId)
                .recipientId(recipientId)
                .items(List.of(
                        new RewardItemRequest().id("1126203259")
                                .type(RewardType.PULL_REQUEST)
                                .number(43L)
                                .repoId(493591124L),
                        new RewardItemRequest().id("995654334")
                                .type(RewardType.PULL_REQUEST)
                                .number(19L)
                                .repoId(493591124L)
                ));

        // When
        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s".formatted(recipientId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        return client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CreateRewardResponse.class)
                .returnResult()
                .getResponseBody()
                .getId();


    }

    private static final String CLOSE_ISSUE_RESPONSE_JSON = """
            {
              "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25",
              "repository_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend",
              "labels_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/labels{/name}",
              "comments_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/comments",
              "events_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/events",
              "html_url": "https://github.com/onlydustxyz/marketplace-frontend/issues/25",
              "id": 1840630179,
              "node_id": "I_kwDOJ4YlT85ttcmj",
              "number": 25,
              "title": "%s",
              "user": {
                "login": "PierreOucif",
                "id": 16590657,
                "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "gravatar_id": "",
                "url": "https://api.github.com/users/PierreOucif",
                "html_url": "https://github.com/PierreOucif",
                "followers_url": "https://api.github.com/users/PierreOucif/followers",
                "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                "repos_url": "https://api.github.com/users/PierreOucif/repos",
                "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                "type": "User",
                "site_admin": false
              },
              "labels": [],
              "state": "closed",
              "locked": false,
              "assignee": null,
              "assignees": [],
              "milestone": null,
              "comments": 0,
              "created_at": "2023-08-08T06:11:35Z",
              "updated_at": "2023-08-08T06:13:08Z",
              "closed_at": "2023-08-08T06:13:08Z",
              "author_association": "MEMBER",
              "active_lock_reason": null,
              "body": "This a body",
              "closed_by": {
                "login": "PierreOucif",
                "id": 16590657,
                "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "gravatar_id": "",
                "url": "https://api.github.com/users/PierreOucif",
                "html_url": "https://github.com/PierreOucif",
                "followers_url": "https://api.github.com/users/PierreOucif/followers",
                "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                "repos_url": "https://api.github.com/users/PierreOucif/repos",
                "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                "type": "User",
                "site_admin": false
              },
              "reactions": {
                "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/reactions",
                "total_count": 0,
                "+1": 0,
                "-1": 0,
                "laugh": 0,
                "hooray": 0,
                "confused": 0,
                "heart": 0,
                "rocket": 0,
                "eyes": 0
              },
              "timeline_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/timeline",
              "performed_via_github_app": null,
              "state_reason": "completed"
            }
            
            """;
    private static final String CREATE_ISSUE_RESPONSE_JSON = """
            {
                                  "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25",
                                  "repository_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend",
                                  "labels_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/labels{/name}",
                                  "comments_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/comments",
                                  "events_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/events",
                                  "html_url": "https://github.com/onlydustxyz/marketplace-frontend/issues/25",
                                  "id": 1840630179,
                                  "node_id": "I_kwDOJ4YlT85ttcmj",
                                  "number": 25,
                                  "title": "%s",
                                  "user": {
                                    "login": "PierreOucif",
                                    "id": 16590657,
                                    "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                                    "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                    "gravatar_id": "",
                                    "url": "https://api.github.com/users/PierreOucif",
                                    "html_url": "https://github.com/PierreOucif",
                                    "followers_url": "https://api.github.com/users/PierreOucif/followers",
                                    "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                                    "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                                    "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                                    "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                                    "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                                    "repos_url": "https://api.github.com/users/PierreOucif/repos",
                                    "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                                    "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                                    "type": "User",
                                    "site_admin": false
                                  },
                                  "labels": [],
                                  "state": "open",
                                  "locked": false,
                                  "assignee": null,
                                  "assignees": [],
                                  "milestone": null,
                                  "comments": 0,
                                  "created_at": "2023-08-08T06:11:35Z",
                                  "updated_at": "2023-08-08T06:11:35Z",
                                  "closed_at": null,
                                  "author_association": "MEMBER",
                                  "active_lock_reason": null,
                                  "body": null,
                                  "closed_by": null,
                                  "reactions": {
                                    "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/reactions",
                                    "total_count": 0,
                                    "+1": 0,
                                    "-1": 0,
                                    "laugh": 0,
                                    "hooray": 0,
                                    "confused": 0,
                                    "heart": 0,
                                    "rocket": 0,
                                    "eyes": 0
                                  },
                                  "timeline_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/timeline",
                                  "performed_via_github_app": null,
                                  "state_reason": null
                                }""";
}
