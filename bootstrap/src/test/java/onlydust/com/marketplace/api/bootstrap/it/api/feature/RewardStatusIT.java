package onlydust.com.marketplace.api.bootstrap.it.api.feature;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Builder;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.api.bootstrap.helper.CurrencyHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.bootstrap.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.project.domain.service.RewardService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RewardStatusIT extends AbstractMarketplaceApiIT {

    @Autowired
    RewardService rewardService;
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountingService accountingService;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    RewardRepository rewardRepository;
    private Long individualBPAdminGithubId = 1L;
    private Long companyBPAdmin1GithubId = 2L;
    private Long companyBPAdmin2GithubId = 3L;
    private Long companyBPMember1GithubId = 4L;
    private Long selfEmployedBPAdminGithubId = 5L;
    private UUID individualBPAdminRewardId;
    private UUID companyBPAdmin1RewardId;
    private UUID companyBPAdmin2RewardId;
    private UUID companyBPMember1RewardId;
    private UUID selfEmployedBPAdminRewardId;
    private UUID projectId;
    private UUID sponsorId = UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d");


    public void setupPendingSignup() {
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [11223344, 55223344],
                          "unlinkedRepoIds": []
                        }
                        """, true, false))
                .willReturn(WireMock.noContent()));

        final var response = client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Project",
                          "shortDescription": "This is a super project",
                          "longDescription": "This is a super awesome project with a nice description",
                          "moreInfos": [
                            {
                              "url": "https://t.me/foobar",
                              "value": "foobar"
                            }
                          ],
                          "isLookingForContributors": true,
                          "inviteGithubUserIdsAsProjectLeads": [],
                          "githubRepoIds": [
                            11223344, 55223344
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                          "ecosystemIds" : ["b599313c-a074-440f-af04-a466529ab2e7","99b6c284-f9bb-4f89-8ce7-03771465ef8e"]
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CreateProjectResponse.class)
                .returnResult().getResponseBody();

        projectId = response.getProjectId();

        final UUID strkId = currencyRepository.findByCode("STRK").orElseThrow().id();
        final SponsorAccountStatement strk = accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(strkId), null,
                new SponsorAccount.Transaction(SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(), Amount.of(100000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        accountingService.allocate(strk.account().id(), ProjectId.of(projectId), PositiveAmount.of(100000L), Currency.Id.of(strkId));

        final var em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        INSERT INTO indexer_exp.github_accounts (id, login, type, html_url, avatar_url, name, tech_created_at, tech_updated_at, bio, location, website, twitter, linkedin, telegram) VALUES (1, 'mmaderic', 'USER', 'https://github.com/mmaderic', 'https://avatars.githubusercontent.com/u/39437117?v=4', 'Mateo Mađerić', '2023-11-21 12:12:48.074041', '2023-11-22 17:33:48.497915', null, 'Croatia', '', null, null, null);
                        INSERT INTO indexer_exp.github_accounts (id, login, type, html_url, avatar_url, name, tech_created_at, tech_updated_at, bio, location, website, twitter, linkedin, telegram) VALUES (2, 'jannesblobel', 'USER', 'https://github.com/jannesblobel', 'https://avatars.githubusercontent.com/u/72493222?v=4', 'Jannes Blobel', '2023-11-09 22:11:21.150640', '2023-11-22 19:47:47.779641', null, null, '', null, null, null);
                        INSERT INTO indexer_exp.github_accounts (id, login, type, html_url, avatar_url, name, tech_created_at, tech_updated_at, bio, location, website, twitter, linkedin, telegram) VALUES (3, 'nickdbush', 'USER', 'https://github.com/nickdbush', 'https://avatars.githubusercontent.com/u/10998201?v=4', 'Nicholas Bush', '2023-11-21 12:12:48.709373', '2023-11-22 17:33:48.580450', 'Building a digital news platform at The Student, Europe''s oldest student newspaper.', 'Edinburgh, UK', 'https://nickdbush.com/', 'https://twitter.com/nickdbush', null, null);
                        INSERT INTO indexer_exp.github_accounts (id, login, type, html_url, avatar_url, name, tech_created_at, tech_updated_at, bio, location, website, twitter, linkedin, telegram) VALUES (4, 'acomminos', 'USER', 'https://github.com/acomminos', 'https://avatars.githubusercontent.com/u/628035?v=4', 'Andrew Comminos', '2023-11-09 22:11:21.150640', '2023-11-22 17:10:10.039392', null, 'San Francisco', 'comminos.com', 'https://twitter.com/acomminos', null, null);
                        INSERT INTO indexer_exp.github_accounts (id, login, type, html_url, avatar_url, name, tech_created_at, tech_updated_at, bio, location, website, twitter, linkedin, telegram) VALUES (5, 'yanns', 'USER', 'https://github.com/yanns', 'https://avatars.githubusercontent.com/u/51669?v=4', 'Yann Simon', '2023-11-09 22:11:21.150640', '2023-11-22 17:13:06.290191', null, 'Berlin', 'https://yanns.github.io/', 'https://twitter.com/simon_yann', null, null);
                        INSERT INTO indexer_exp.github_repos (id, owner_id, name, html_url, updated_at, description, stars_count, forks_count, has_issues, parent_id, tech_created_at, tech_updated_at, owner_login, visibility) VALUES (11223344, 98735558, 'account-obstr-2', 'https://github.com/onlydustxyz/account-obstr', '2022-11-01 18:27:14.000000', null, 0, 0, true, null, '2023-11-22 14:19:27.975872', '2023-12-04 14:24:00.541641', 'onlydustxyz', 'PUBLIC');
                        INSERT INTO indexer_exp.github_repos (id, owner_id, name, html_url, updated_at, description, stars_count, forks_count, has_issues, parent_id, tech_created_at, tech_updated_at, owner_login, visibility) VALUES (55223344, 98735558, 'marketplace-provisionning-2', 'https://github.com/onlydustxyz/marketplace-provisionning', '2023-07-31 08:56:57.000000', null, 0, 0, true, null, '2023-11-22 14:19:27.975893', '2023-12-04 14:24:01.809884', 'onlydustxyz', 'PUBLIC');
                        INSERT INTO indexer_exp.github_pull_requests (id, repo_id, number, title, status, created_at, closed_at, merged_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, draft, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url, review_state, commit_count) VALUES (0011051356, 55223344, 1, 'fix issue link query', 'MERGED', '2023-11-21 14:13:35.000000', '2023-11-21 14:27:21.000000', '2023-11-21 14:27:21.000000', 1, 'https://github.com/onlydustxyz/marketplace-api/pull/128', null, 0, '2023-11-21 15:17:17.139895', '2023-11-22 17:49:23.008254', false, 'onlydustxyz', 'marketplace-api', 'https://github.com/onlydustxyz/marketplace-api', 'AnthonyBuisset', 'https://github.com/AnthonyBuisset', 'https://avatars.githubusercontent.com/u/43467246?v=4', 'PENDING_REVIEWER', 1);
                        INSERT INTO indexer_exp.github_pull_requests (id, repo_id, number, title, status, created_at, closed_at, merged_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, draft, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url, review_state, commit_count) VALUES (0001051356, 11223344, 1, 'fix issue link query', 'MERGED', '2023-11-21 14:13:35.000000', '2023-11-21 14:27:21.000000', '2023-11-21 14:27:21.000000', 1, 'https://github.com/onlydustxyz/marketplace-api/pull/128', null, 0, '2023-11-21 15:17:17.139895', '2023-11-22 17:49:23.008254', false, 'onlydustxyz', 'marketplace-api', 'https://github.com/onlydustxyz/marketplace-api', 'AnthonyBuisset', 'https://github.com/AnthonyBuisset', 'https://avatars.githubusercontent.com/u/43467246?v=4', 'PENDING_REVIEWER', 1);
                        """)
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();

        sendRewardToRecipient(individualBPAdminGithubId, 10L);
        sendRewardToRecipient(companyBPAdmin1GithubId, 20L);
        sendRewardToRecipient(companyBPAdmin2GithubId, 30L);
        sendRewardToRecipient(companyBPMember1GithubId, 40L);
        sendRewardToRecipient(selfEmployedBPAdminGithubId, 50L);
        setUp();
    }

    void setUp() {
        final ProjectResponse project = client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/super-project"))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectResponse.class)
                .returnResult()
                .getResponseBody();

        projectId = project.getId();
        final List<RewardEntity> allRewards = rewardRepository.findAll();

        individualBPAdminRewardId =
                allRewards.stream().filter(r -> r.projectId().equals(projectId) && r.recipientId().equals(individualBPAdminGithubId)).findFirst().orElseThrow().id();
        companyBPAdmin1RewardId =
                allRewards.stream().filter(r -> r.projectId().equals(projectId) && r.recipientId().equals(companyBPAdmin1GithubId)).findFirst().orElseThrow().id();
        companyBPAdmin2RewardId =
                allRewards.stream().filter(r -> r.projectId().equals(projectId) && r.recipientId().equals(companyBPAdmin2GithubId)).findFirst().orElseThrow().id();
        companyBPMember1RewardId =
                allRewards.stream().filter(r -> r.projectId().equals(projectId) && r.recipientId().equals(companyBPMember1GithubId)).findFirst().orElseThrow().id();
        selfEmployedBPAdminRewardId =
                allRewards.stream().filter(r -> r.projectId().equals(projectId) && r.recipientId().equals(selfEmployedBPAdminGithubId)).findFirst().orElseThrow().id();
    }

    private void sendRewardToRecipient(Long recipientId, Long amount) {
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(amount))
                .currencyId(CurrencyHelper.STRK.value())
                .recipientId(recipientId)
                .items(List.of(
                        new RewardItemRequest().id("0011051356")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(55223344L),
                        new RewardItemRequest().id("0001051356")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(11223344L)
                ));

        // When
        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s".formatted(recipientId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();


    }

    @Test
    @Order(1)
    public void should_display_reward_statuses_given_pending_signup_statuses() {
        // Given
        setupPendingSignup();

        // When
        assertGetProjectRewardsStatus(
                Map.of(
                        individualBPAdminRewardId, "PENDING_SIGNUP",
                        companyBPAdmin1RewardId, "PENDING_SIGNUP",
                        companyBPAdmin2RewardId, "PENDING_SIGNUP",
                        companyBPMember1RewardId, "PENDING_SIGNUP",
                        selfEmployedBPAdminRewardId, "PENDING_SIGNUP"
                )
        );
    }

    @Test
    @Order(20)
    void should_display_reward_statuses_given_pending_billing_profile() {
        // Given
        setUp();
        userAuthHelper.signUpUser(UUID.randomUUID(), 1L, "mmaderic", "https://avatars.githubusercontent.com/u/39437117?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 2L, "jannesblobel", "https://avatars.githubusercontent.com/u/72493222?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 3L, "nickdbush", "https://avatars.githubusercontent.com/u/628035?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 4L, "acomminos", "https://avatars.githubusercontent.com/u/628035?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 5L, "yanns", "https://avatars.githubusercontent.com/u/51669?v=4", false);

        // When
        assertGetProjectRewardsStatus(
                Map.of(
                        individualBPAdminRewardId, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId, "PENDING_CONTRIBUTOR"
                )
        );

        // When
        assertGetMyRewardsStatus(List.of(
                MyRewardDatum.builder() 
                        .githubUserId(individualBPAdminGithubId)
                        .rewardId(individualBPAdminRewardId)
                        .status("PENDING_BILLING_PROFILE")
                        .build()
        ));
    }

    private void assertGetMyRewardsStatus(final List<MyRewardDatum> myRewardData) {
        for (MyRewardDatum myRewardDatum : myRewardData) {
            final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateUser(myRewardDatum.githubUserId);
            client.get()
                    .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "10")))
                    .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(myRewardDatum.rewardId.toString())).isEqualTo(myRewardDatum.status)
                    .jsonPath("$.rewards.length()").isEqualTo(1)
                    .jsonPath("$.rewardedAmount.amount").isEqualTo(myRewardDatum.rewardedAmount)
                    .jsonPath("$.pendingAmount.amount").isEqualTo(myRewardDatum.pendingAmount)
                    .json(GET_MY_REWARDS_JSON_RESPONSE);
        }
    }

    @Builder
    private record MyRewardDatum(Long githubUserId, UUID rewardId, String status, Long rewardedAmount, Long pendingAmount) {
    }

    ;

    private void assertGetProjectRewardsStatus(final Map<UUID, String> rewardStatusMapToId) {
        // When
        client.get()
                .uri(getApiURI(PROJECTS_REWARDS.formatted(projectId), Map.of("pageIndex", "0", "pageSize", "20")))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticatePierre().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(5)
                .json(GET_PROJECT_REWARDS_PENDING_SIGNUP_JSON_RESPONSE)
                .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(individualBPAdminRewardId.toString())).isEqualTo(rewardStatusMapToId.get(individualBPAdminRewardId))
                .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPAdmin1RewardId.toString())).isEqualTo(rewardStatusMapToId.get(companyBPAdmin1RewardId))
                .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPAdmin2RewardId.toString())).isEqualTo(rewardStatusMapToId.get(companyBPAdmin2RewardId))
                .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPMember1RewardId.toString())).isEqualTo(rewardStatusMapToId.get(companyBPMember1RewardId))
                .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(selfEmployedBPAdminRewardId.toString())).isEqualTo(rewardStatusMapToId.get(selfEmployedBPAdminRewardId));

        // When
        for (Map.Entry<UUID, String> rewardIdStatus : rewardStatusMapToId.entrySet()) {
            client.get()
                    .uri(getApiURI(PROJECTS_REWARD.formatted(projectId, rewardIdStatus.getKey().toString())))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticatePierre().jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json(getProjectRewardResponseById(rewardIdStatus.getKey()))
                    .jsonPath("$.status").isEqualTo(rewardIdStatus.getValue());
        }
    }

    private String getProjectRewardResponseById(final UUID rewardId) {
        if (rewardId.equals(individualBPAdminRewardId)) {
            return GET_PROJECT_INDIVIDUAL_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPAdmin1RewardId)) {
            return GET_PROJECT_COMPANY_ADMIN_1_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPAdmin2RewardId)) {
            return GET_PROJECT_COMPANY_ADMIN_2_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPMember1RewardId)) {
            return GET_PROJECT_COMPANY_MEMBER_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(selfEmployedBPAdminRewardId)) {
            return GET_PROJECT_SELF_EMPLOYED_REWARD_JSON_RESPONSE;
        }
        throw new RuntimeException("Invalid rewardId");
    }

    private static final String GET_PROJECT_REWARDS_PENDING_SIGNUP_JSON_RESPONSE = """
            {
              "rewards": [
                {
                  "processedAt": null,
                  "unlockDate": null,
                  "amount": {
                    "total": 10,
                    "currency": {
                      "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                      "code": "STRK",
                      "name": "StarkNet Token",
                      "logoUrl": null,
                      "decimals": 18
                    },
                    "dollarsEquivalent": null
                  },
                  "numberOfRewardedContributions": 2,
                  "rewardedUserLogin": "mmaderic",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/39437117?v=4"
                },
                {
                  "processedAt": null,
                  "unlockDate": null,
                  "amount": {
                    "total": 20,
                    "currency": {
                      "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                      "code": "STRK",
                      "name": "StarkNet Token",
                      "logoUrl": null,
                      "decimals": 18
                    },
                    "dollarsEquivalent": null
                  },
                  "numberOfRewardedContributions": 2,
                  "rewardedUserLogin": "jannesblobel",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/72493222?v=4"
                },
                {
                  "processedAt": null,
                  "unlockDate": null,
                  "amount": {
                    "total": 30,
                    "currency": {
                      "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                      "code": "STRK",
                      "name": "StarkNet Token",
                      "logoUrl": null,
                      "decimals": 18
                    },
                    "dollarsEquivalent": null
                  },
                  "numberOfRewardedContributions": 2,
                  "rewardedUserLogin": "nickdbush",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/10998201?v=4"
                },
                {
                  "processedAt": null,
                  "unlockDate": null,
                  "amount": {
                    "total": 40,
                    "currency": {
                      "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                      "code": "STRK",
                      "name": "StarkNet Token",
                      "logoUrl": null,
                      "decimals": 18
                    },
                    "dollarsEquivalent": null
                  },
                  "numberOfRewardedContributions": 2,
                  "rewardedUserLogin": "acomminos",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/628035?v=4"
                },
                {
                  "processedAt": null,
                  "unlockDate": null,
                  "amount": {
                    "total": 50,
                    "currency": {
                      "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                      "code": "STRK",
                      "name": "StarkNet Token",
                      "logoUrl": null,
                      "decimals": 18
                    },
                    "dollarsEquivalent": null
                  },
                  "numberOfRewardedContributions": 2,
                  "rewardedUserLogin": "yanns",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/51669?v=4"
                }
              ],
              "remainingBudget": {
                "amount": 99850,
                "currency": {
                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                  "code": "STRK",
                  "name": "StarkNet Token",
                  "logoUrl": null,
                  "decimals": 18
                },
                "usdEquivalent": null
              },
              "spentAmount": {
                "amount": 150,
                "currency": {
                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                  "code": "STRK",
                  "name": "StarkNet Token",
                  "logoUrl": null,
                  "decimals": 18
                },
                "usdEquivalent": null
              },
              "sentRewardsCount": 5,
              "rewardedContributionsCount": 2,
              "rewardedContributorsCount": 5,
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 5,
              "nextPageIndex": 0
            }
            """;

    private static final String GET_PROJECT_INDIVIDUAL_REWARD_JSON_RESPONSE = """
            {
              "currency": {
                "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                "code": "STRK",
                "name": "StarkNet Token",
                "logoUrl": null,
                "decimals": 18
              },
              "amount": 10,
              "dollarsEquivalent": null,
              "unlockDate": null,
              "from": {
                "githubUserId": 16590657,
                "login": "PierreOucif",
                "htmlUrl": null,
                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "isRegistered": null
              },
              "to": {
                "githubUserId": 1,
                "login": "mmaderic",
                "htmlUrl": null,
                "avatarUrl": "https://avatars.githubusercontent.com/u/39437117?v=4",
                "isRegistered": null
              },
              "processedAt": null,
              "project": {
                "slug": "super-project",
                "name": "Super Project",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              },
              "receipt": null
            }
            """;

    private static final String GET_PROJECT_COMPANY_ADMIN_1_REWARD_JSON_RESPONSE = """
            {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 20,
               "dollarsEquivalent": null,
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": null,
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": null
               },
               "to": {
                 "githubUserId": 2,
                 "login": "jannesblobel",
                 "htmlUrl": null,
                 "avatarUrl": "https://avatars.githubusercontent.com/u/72493222?v=4",
                 "isRegistered": null
               },
               "processedAt": null,
               "project": {
                 "slug": "super-project",
                 "name": "Super Project",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;

    private static final String GET_PROJECT_COMPANY_ADMIN_2_REWARD_JSON_RESPONSE = """
             {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 30,
               "dollarsEquivalent": null,
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": null,
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": null
               },
               "to": {
                 "githubUserId": 3,
                 "login": "nickdbush",
                 "htmlUrl": null,
                 "avatarUrl": "https://avatars.githubusercontent.com/u/10998201?v=4",
                 "isRegistered": null
               },
               "processedAt": null,
               "project": {
                 "slug": "super-project",
                 "name": "Super Project",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;


    private static final String GET_PROJECT_SELF_EMPLOYED_REWARD_JSON_RESPONSE = """
            {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 50,
               "dollarsEquivalent": null,
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": null,
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": null
               },
               "to": {
                 "githubUserId": 5,
                 "login": "yanns",
                 "htmlUrl": null,
                 "avatarUrl": "https://avatars.githubusercontent.com/u/51669?v=4",
                 "isRegistered": null
               },
               "processedAt": null,
               "project": {
                 "slug": "super-project",
                 "name": "Super Project",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;

    private static final String GET_PROJECT_COMPANY_MEMBER_REWARD_JSON_RESPONSE = """
            {
              "currency": {
                "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                "code": "STRK",
                "name": "StarkNet Token",
                "logoUrl": null,
                "decimals": 18
              },
              "amount": 40,
              "dollarsEquivalent": null,
              "unlockDate": null,
              "from": {
                "githubUserId": 16590657,
                "login": "PierreOucif",
                "htmlUrl": null,
                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "isRegistered": null
              },
              "to": {
                "githubUserId": 4,
                "login": "acomminos",
                "htmlUrl": null,
                "avatarUrl": "https://avatars.githubusercontent.com/u/628035?v=4",
                "isRegistered": null
              },
              "processedAt": null,
              "project": {
                "slug": "super-project",
                "name": "Super Project",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              },
              "receipt": null
            }
            """;

    private static final String GET_MY_REWARDS_JSON_RESPONSE = """
            {
                          "rewards": [
                            {
                              "processedAt": null,
                              "unlockDate": null,
                              "amount": {
                                "total": 10,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "dollarsEquivalent": null
                              },
                              "numberOfRewardedContributions": 2,
                              "rewardedOnProjectName": "Super Project",
                              "rewardedOnProjectLogoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "nextPageIndex": 0,
                          "rewardedAmount": {
                            "currency": {
                              "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                              "code": "STRK",
                              "name": "StarkNet Token",
                              "logoUrl": null,
                              "decimals": 18
                            },
                            "usdEquivalent": 0
                          },
                          "pendingAmount": {
                            "currency": {
                              "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                              "code": "STRK",
                              "name": "StarkNet Token",
                              "logoUrl": null,
                              "decimals": 18
                            },
                            "usdEquivalent": 0
                          },
                          "receivedRewardsCount": 1,
                          "rewardedContributionsCount": 2,
                          "rewardingProjectsCount": 1
                        }
            """;
}
