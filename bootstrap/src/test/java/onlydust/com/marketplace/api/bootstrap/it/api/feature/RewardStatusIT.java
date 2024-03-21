package onlydust.com.marketplace.api.bootstrap.it.api.feature;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.util.Objects.isNull;
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
    @Autowired
    BillingProfileService billingProfileService;
    private final Long individualBPAdminGithubId = 1L;
    private final Long companyBPAdmin1GithubId = 2L;
    private final Long companyBPAdmin2GithubId = 3L;
    private final Long companyBPMember1GithubId = 4L;
    private final Long selfEmployedBPAdminGithubId = 5L;
    private UUID individualBPAdminId;
    private UUID companyBPAdmin1Id;
    private UUID companyBPAdmin2Id;
    private UUID companyBPMember1Id;
    private UUID selfEmployedBPAdminId;
    private UUID individualBPAdminRewardId1;
    private UUID individualBPAdminRewardId2;
    private UUID companyBPAdmin1RewardId1;
    private UUID companyBPAdmin1RewardId2;
    private UUID companyBPAdmin2RewardId1;
    private UUID companyBPAdmin2RewardId2;
    private UUID companyBPMember1RewardId1;
    private UUID companyBPMember1RewardId2;
    private UUID selfEmployedBPAdminRewardId1;
    private UUID selfEmployedBPAdminRewardId2;
    private UUID projectId1;
    private UUID projectId2;
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

        final var response1 = client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Project 1",
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


        projectId1 = response1.getProjectId();

        final var response2 = client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Project 2",
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


        projectId2 = response2.getProjectId();


        final UUID strkId = currencyRepository.findByCode("STRK").orElseThrow().id();
        final SponsorAccountStatement strk = accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(strkId), null,
                new SponsorAccount.Transaction(SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(), Amount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        accountingService.allocate(strk.account().id(), ProjectId.of(projectId1), PositiveAmount.of(100000L), Currency.Id.of(strkId));
        accountingService.allocate(strk.account().id(), ProjectId.of(projectId2), PositiveAmount.of(100000L), Currency.Id.of(strkId));

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

        sendRewardToRecipient(individualBPAdminGithubId, 10L, projectId1);
        sendRewardToRecipient(individualBPAdminGithubId, 100L, projectId2);
        sendRewardToRecipient(companyBPAdmin1GithubId, 20L, projectId1);
        sendRewardToRecipient(companyBPAdmin1GithubId, 200L, projectId2);
        sendRewardToRecipient(companyBPAdmin2GithubId, 30L, projectId1);
        sendRewardToRecipient(companyBPAdmin2GithubId, 300L, projectId2);
        sendRewardToRecipient(companyBPMember1GithubId, 40L, projectId1);
        sendRewardToRecipient(companyBPMember1GithubId, 400L, projectId2);
        sendRewardToRecipient(selfEmployedBPAdminGithubId, 50L, projectId1);
        sendRewardToRecipient(selfEmployedBPAdminGithubId, 500L, projectId2);
        setUp();
    }

    public void setupPendingBillingProfile() {
        setUp();
        userAuthHelper.signUpUser(UUID.randomUUID(), 1L, "mmaderic", "https://avatars.githubusercontent.com/u/39437117?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 2L, "jannesblobel", "https://avatars.githubusercontent.com/u/72493222?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 3L, "nickdbush", "https://avatars.githubusercontent.com/u/628035?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 4L, "acomminos", "https://avatars.githubusercontent.com/u/628035?v=4", false);
        userAuthHelper.signUpUser(UUID.randomUUID(), 5L, "yanns", "https://avatars.githubusercontent.com/u/51669?v=4", false);
    }


    private void updatePayoutPreferences(final Long githubUserId, BillingProfile.Id billingProfileId, final UUID projectId) {
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateUser(githubUserId);
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(isNull(billingProfileId) ? null : billingProfileId.value(), projectId))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    void setUp() {
        projectId1 = client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/super-project-1"))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectResponse.class)
                .returnResult()
                .getResponseBody().getId();

        projectId2 = client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/super-project-2"))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectResponse.class)
                .returnResult()
                .getResponseBody().getId();

        final List<RewardEntity> allRewards = rewardRepository.findAll();

        individualBPAdminRewardId1 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId1) && r.recipientId().equals(individualBPAdminGithubId)).findFirst().orElseThrow().id();
        companyBPAdmin1RewardId1 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId1) && r.recipientId().equals(companyBPAdmin1GithubId)).findFirst().orElseThrow().id();
        companyBPAdmin2RewardId1 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId1) && r.recipientId().equals(companyBPAdmin2GithubId)).findFirst().orElseThrow().id();
        companyBPMember1RewardId1 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId1) && r.recipientId().equals(companyBPMember1GithubId)).findFirst().orElseThrow().id();
        selfEmployedBPAdminRewardId1 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId1) && r.recipientId().equals(selfEmployedBPAdminGithubId)).findFirst().orElseThrow().id();

        individualBPAdminRewardId2 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId2) && r.recipientId().equals(individualBPAdminGithubId)).findFirst().orElseThrow().id();
        companyBPAdmin1RewardId2 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId2) && r.recipientId().equals(companyBPAdmin1GithubId)).findFirst().orElseThrow().id();
        companyBPAdmin2RewardId2 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId2) && r.recipientId().equals(companyBPAdmin2GithubId)).findFirst().orElseThrow().id();
        companyBPMember1RewardId2 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId2) && r.recipientId().equals(companyBPMember1GithubId)).findFirst().orElseThrow().id();
        selfEmployedBPAdminRewardId2 =
                allRewards.stream().filter(r -> r.projectId().equals(projectId2) && r.recipientId().equals(selfEmployedBPAdminGithubId)).findFirst().orElseThrow().id();

        userRepository.findByGithubUserId(individualBPAdminGithubId).ifPresent(userEntity -> individualBPAdminId = userEntity.getId());
        userRepository.findByGithubUserId(companyBPAdmin1GithubId).ifPresent(userEntity -> companyBPAdmin1Id = userEntity.getId());
        userRepository.findByGithubUserId(companyBPAdmin2GithubId).ifPresent(userEntity -> companyBPAdmin2Id = userEntity.getId());
        userRepository.findByGithubUserId(companyBPMember1GithubId).ifPresent(userEntity -> companyBPMember1Id = userEntity.getId());
        userRepository.findByGithubUserId(selfEmployedBPAdminGithubId).ifPresent(userEntity -> selfEmployedBPAdminId = userEntity.getId());
    }

    private void sendRewardToRecipient(Long recipientId, Long amount, UUID projectId) {
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
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_SIGNUP",
                        companyBPAdmin1RewardId1, "PENDING_SIGNUP",
                        companyBPAdmin2RewardId1, "PENDING_SIGNUP",
                        companyBPMember1RewardId1, "PENDING_SIGNUP",
                        selfEmployedBPAdminRewardId1, "PENDING_SIGNUP"
                )
        );
        assertGetProjectRewardsStatusOnProject(
                projectId2,
                Map.of(
                        individualBPAdminRewardId2, "PENDING_SIGNUP",
                        companyBPAdmin1RewardId2, "PENDING_SIGNUP",
                        companyBPAdmin2RewardId2, "PENDING_SIGNUP",
                        companyBPMember1RewardId2, "PENDING_SIGNUP",
                        selfEmployedBPAdminRewardId2, "PENDING_SIGNUP"
                )
        );

    }

    @Test
    @Order(20)
    void should_display_reward_statuses_given_pending_billing_profile() {
        // Given
        setupPendingBillingProfile();

        // When
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId1, "PENDING_CONTRIBUTOR"
                )
        );
        assertGetProjectRewardsStatusOnProject(
                projectId2,
                Map.of(
                        individualBPAdminRewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId2, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId2, "PENDING_CONTRIBUTOR"
                )
        );

        // When
        assertGetMyRewardsStatus(List.of(
                MyRewardDatum.builder()
                        .githubUserId(individualBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .build()
                        ))
                        .build()
        ));
    }

    @Test
    @Order(30)
    void should_display_reward_statuses_given_pending_pending_verification() {
        // Given
        setUp();

        final IndividualBillingProfile individualBillingProfile = billingProfileService.createIndividualBillingProfile(UserId.of(individualBPAdminId),
                faker.rickAndMorty().character(), null);

        final CompanyBillingProfile companyBillingProfile = billingProfileService.createCompanyBillingProfile(UserId.of(companyBPAdmin1Id),
                faker.gameOfThrones().character(), null);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(companyBPAdmin1Id), GithubUserId.of(companyBPAdmin2GithubId),
                BillingProfile.User.Role.ADMIN);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(companyBPAdmin1Id), GithubUserId.of(companyBPMember1GithubId),
                BillingProfile.User.Role.MEMBER);
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(companyBPAdmin2GithubId));
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(companyBPMember1GithubId));

        final SelfEmployedBillingProfile selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(UserId.of(selfEmployedBPAdminId)
                , faker.lordOfTheRings().character(), null);


        updatePayoutPreferences(individualBPAdminGithubId, individualBillingProfile.id(), projectId1);

        // When
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId1, "PENDING_CONTRIBUTOR"
                )
        );
        assertGetProjectRewardsStatusOnProject(
                projectId2,
                Map.of(
                        individualBPAdminRewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId2, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId2, "PENDING_CONTRIBUTOR"
                )
        );

        // When
        assertGetMyRewardsStatus(List.of(
                MyRewardDatum.builder()
                        .githubUserId(individualBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .build()
                        ))
                        .build()
        ));


        updatePayoutPreferences(companyBPAdmin1GithubId, companyBillingProfile.id(), projectId1);

        // When
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId1, "PENDING_CONTRIBUTOR"
                )
        );
        assertGetProjectRewardsStatusOnProject(
                projectId2,
                Map.of(
                        individualBPAdminRewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId2, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId2, "PENDING_CONTRIBUTOR"
                )
        );

        // When
        assertGetMyRewardsStatus(List.of(
                MyRewardDatum.builder()
                        .githubUserId(individualBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .build()
                        ))
                        .build()
        ));

        updatePayoutPreferences(companyBPAdmin2GithubId, companyBillingProfile.id(), projectId1);

        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId1, "PENDING_CONTRIBUTOR"
                )
        );
        assertGetProjectRewardsStatusOnProject(
                projectId2,
                Map.of(
                        individualBPAdminRewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId2, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId2, "PENDING_CONTRIBUTOR"
                )
        );

        assertGetMyRewardsStatus(List.of(
                MyRewardDatum.builder()
                        .githubUserId(individualBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .build()
                        ))
                        .build()
        ));


        updatePayoutPreferences(companyBPMember1GithubId, companyBillingProfile.id(), projectId1);

        // When
        assertGetMyRewardsStatus(List.of(
                MyRewardDatum.builder()
                        .githubUserId(individualBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_COMPANY")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .build()
                        ))
                        .build()
        ));

        updatePayoutPreferences(selfEmployedBPAdminGithubId, selfEmployedBillingProfile.id(), projectId1);

        // When
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId1, "PENDING_CONTRIBUTOR"
                )
        );
        assertGetProjectRewardsStatusOnProject(
                projectId2,
                Map.of(
                        individualBPAdminRewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId2, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId2, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId2, "PENDING_CONTRIBUTOR"
                )
        );

        // When
        assertGetMyRewardsStatus(List.of(
                MyRewardDatum.builder()
                        .githubUserId(individualBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_COMPANY")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .build()
                        ))
                        .build()
        ));
    }

    private void assertGetMyRewardsStatus(final List<MyRewardDatum> myRewardData) {
        for (MyRewardDatum myRewardDatum : myRewardData) {
            final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateUser(myRewardDatum.githubUserId);


            final WebTestClient.BodyContentSpec bodyContentSpec = client.get()
                    .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "10")))
                    .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .consumeWith(System.out::println)
                    .jsonPath("$.rewards.length()").isEqualTo(myRewardDatum.rewardData.size());

            long rewardedAmount = 0;
            long pendingAmount = 0;

            for (RewardDatum rewardDatum : myRewardDatum.rewardData()) {
                bodyContentSpec
                        .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(myRewardDatum.rewardData.get(0).rewardId.toString()))
                        .isEqualTo(myRewardDatum.rewardData.get(0).status);
                rewardedAmount += rewardDatum.rewardedAmount;
                pendingAmount += rewardDatum.pendingAmount;

                client.get()
                        .uri(getApiURI(ME_REWARD.formatted(rewardDatum.rewardId.toString())))
                        .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                        // Then
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody()
                        .consumeWith(System.out::println)
                        .jsonPath("$.status").isEqualTo(rewardDatum.status)
                        .jsonPath("$.amount").isEqualTo(rewardDatum.rewardedAmount);
            }


            bodyContentSpec.jsonPath("$.rewardedAmount.amount").isEqualTo(rewardedAmount)
                    .jsonPath("$.pendingAmount.amount").isEqualTo(pendingAmount);

        }
    }

    @Builder
    private record MyRewardDatum(@NonNull Long githubUserId, @NonNull List<RewardDatum> rewardData) {
    }

    @Builder
    private record RewardDatum(@NonNull UUID rewardId, @NonNull String status, @NonNull Long rewardedAmount, @NonNull Long pendingAmount) {
    }

    private void assertGetProjectRewardsStatusOnProject(final UUID projectId, final Map<UUID, String> rewardStatusMapToId) {
        // When
        if (projectId.equals(projectId1)) {
            client.get()
                    .uri(getApiURI(PROJECTS_REWARDS.formatted(projectId), Map.of("pageIndex", "0", "pageSize", "20")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticatePierre().jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.rewards.size()").isEqualTo(5)
                    .json(GET_PROJECT_1_REWARDS_PENDING_SIGNUP_JSON_RESPONSE)
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(individualBPAdminRewardId1.toString())).isEqualTo(rewardStatusMapToId.get(individualBPAdminRewardId1))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPAdmin1RewardId1.toString())).isEqualTo(rewardStatusMapToId.get(companyBPAdmin1RewardId1))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPAdmin2RewardId1.toString())).isEqualTo(rewardStatusMapToId.get(companyBPAdmin2RewardId1))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPMember1RewardId1.toString())).isEqualTo(rewardStatusMapToId.get(companyBPMember1RewardId1))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(selfEmployedBPAdminRewardId1.toString())).isEqualTo(rewardStatusMapToId.get(selfEmployedBPAdminRewardId1));
        } else {
            client.get()
                    .uri(getApiURI(PROJECTS_REWARDS.formatted(projectId), Map.of("pageIndex", "0", "pageSize", "20")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticatePierre().jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.rewards.size()").isEqualTo(5)
                    .json(GET_PROJECT_2_REWARDS_PENDING_SIGNUP_JSON_RESPONSE)
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(individualBPAdminRewardId2.toString())).isEqualTo(rewardStatusMapToId.get(individualBPAdminRewardId2))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPAdmin1RewardId2.toString())).isEqualTo(rewardStatusMapToId.get(companyBPAdmin1RewardId2))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPAdmin2RewardId2.toString())).isEqualTo(rewardStatusMapToId.get(companyBPAdmin2RewardId2))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(companyBPMember1RewardId2.toString())).isEqualTo(rewardStatusMapToId.get(companyBPMember1RewardId2))
                    .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(selfEmployedBPAdminRewardId2.toString())).isEqualTo(rewardStatusMapToId.get(selfEmployedBPAdminRewardId2));
        }

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
        if (rewardId.equals(individualBPAdminRewardId1)) {
            return GET_PROJECT_1_INDIVIDUAL_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPAdmin1RewardId1)) {
            return GET_PROJECT_1_COMPANY_ADMIN_1_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPAdmin2RewardId1)) {
            return GET_PROJECT_1_COMPANY_ADMIN_2_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPMember1RewardId1)) {
            return GET_PROJECT_1_COMPANY_MEMBER_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(selfEmployedBPAdminRewardId1)) {
            return GET_PROJECT_1_SELF_EMPLOYED_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(individualBPAdminRewardId2)) {
            return GET_PROJECT_2_INDIVIDUAL_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPAdmin1RewardId2)) {
            return GET_PROJECT_2_COMPANY_ADMIN_1_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPAdmin2RewardId2)) {
            return GET_PROJECT_2_COMPANY_ADMIN_2_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(companyBPMember1RewardId2)) {
            return GET_PROJECT_2_COMPANY_MEMBER_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(selfEmployedBPAdminRewardId2)) {
            return GET_PROJECT_2_SELF_EMPLOYED_REWARD_JSON_RESPONSE;
        }
        throw new RuntimeException("Invalid rewardId");
    }

    private static final String GET_PROJECT_1_REWARDS_PENDING_SIGNUP_JSON_RESPONSE = """
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

    private static final String GET_PROJECT_2_REWARDS_PENDING_SIGNUP_JSON_RESPONSE = """
            {
              "rewards": [
                {
                  "processedAt": null,
                  "unlockDate": null,
                  "amount": {
                    "total": 100,
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
                    "total": 200,
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
                    "total": 300,
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
                    "total": 400,
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
                    "total": 500,
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
                "amount": 98500,
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
                "amount": 1500,
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


    private static final String GET_PROJECT_1_INDIVIDUAL_REWARD_JSON_RESPONSE = """
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
                "slug": "super-project-1",
                "name": "Super Project 1",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              },
              "receipt": null
            }
            """;
    private static final String GET_PROJECT_2_INDIVIDUAL_REWARD_JSON_RESPONSE = """
            {
              "currency": {
                "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                "code": "STRK",
                "name": "StarkNet Token",
                "logoUrl": null,
                "decimals": 18
              },
              "amount": 100,
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
                "slug": "super-project-2",
                "name": "Super Project 2",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              },
              "receipt": null
            }
            """;


    private static final String GET_PROJECT_1_COMPANY_ADMIN_1_REWARD_JSON_RESPONSE = """
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
                 "slug": "super-project-1",
                 "name": "Super Project 1",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;

    private static final String GET_PROJECT_2_COMPANY_ADMIN_1_REWARD_JSON_RESPONSE = """
            {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 200,
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
                 "slug": "super-project-2",
                 "name": "Super Project 2",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;

    private static final String GET_PROJECT_1_COMPANY_ADMIN_2_REWARD_JSON_RESPONSE = """
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
                 "slug": "super-project-1",
                 "name": "Super Project 1",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;

    private static final String GET_PROJECT_2_COMPANY_ADMIN_2_REWARD_JSON_RESPONSE = """
             {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 300,
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
                 "slug": "super-project-2",
                 "name": "Super Project 2",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;

    private static final String GET_PROJECT_1_SELF_EMPLOYED_REWARD_JSON_RESPONSE = """
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
                 "slug": "super-project-1",
                 "name": "Super Project 1",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;

    private static final String GET_PROJECT_2_SELF_EMPLOYED_REWARD_JSON_RESPONSE = """
            {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 500,
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
                 "slug": "super-project-2",
                 "name": "Super Project 2",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               },
               "receipt": null
             }
            """;


    private static final String GET_PROJECT_1_COMPANY_MEMBER_REWARD_JSON_RESPONSE = """
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
                "slug": "super-project-1",
                "name": "Super Project 1",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              },
              "receipt": null
            }
            """;

    private static final String GET_PROJECT_2_COMPANY_MEMBER_REWARD_JSON_RESPONSE = """
            {
              "currency": {
                "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                "code": "STRK",
                "name": "StarkNet Token",
                "logoUrl": null,
                "decimals": 18
              },
              "amount": 400,
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
                "slug": "super-project-2",
                "name": "Super Project 2",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              },
              "receipt": null
            }
            """;
}

