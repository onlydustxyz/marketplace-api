package onlydust.com.marketplace.api.bootstrap.it.api.feature;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.PayRewardRequest;
import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.*;
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.bootstrap.helper.CurrencyHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.bootstrap.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeAccountingManagementRestApi;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
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
import org.testcontainers.shaded.org.apache.commons.lang3.mutable.MutableObject;

import javax.persistence.EntityManagerFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

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
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    QuoteStorage quoteStorage;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    AccountingObserver accountingObserver;
    @Autowired
    InvoiceService invoiceService;

    private final Double strkToUsd1 = 2.5;
    private final Double strkToUsd2 = 3.4;
    private final Double strkToUsd3 = 0.0001;


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
    // 12 for project1 reward1
    private UUID selfEmployedBPAdminRewardId11;
    // 12 for project1 reward2
    private UUID selfEmployedBPAdminRewardId12;
    private UUID selfEmployedBPAdminRewardId2;
    private UUID projectId1;
    private UUID projectId2;
    private UUID sponsorId = UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d");
    private UUID individualBPId;
    private UUID companyBPId;
    private UUID selfEmployedBPId;

    public void resetAuth0Mock() {
        userRepository.findByGithubUserId(individualBPAdminGithubId).ifPresent(userAuthHelper::mockAuth0UserInfo);
        userRepository.findByGithubUserId(companyBPAdmin1GithubId).ifPresent(userAuthHelper::mockAuth0UserInfo);
        userRepository.findByGithubUserId(companyBPAdmin2GithubId).ifPresent(userAuthHelper::mockAuth0UserInfo);
        userRepository.findByGithubUserId(companyBPMember1GithubId).ifPresent(userAuthHelper::mockAuth0UserInfo);
        userRepository.findByGithubUserId(selfEmployedBPAdminGithubId).ifPresent(userAuthHelper::mockAuth0UserInfo);
        userRepository.findByGithubUserId(16590657L).ifPresent(userAuthHelper::mockAuth0UserInfo);
    }

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
        final SponsorAccountStatement strkSponsorAccount = accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(strkId), null,
                new SponsorAccount.Transaction(SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(), Amount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        accountingService.allocate(strkSponsorAccount.account().id(), ProjectId.of(projectId1), PositiveAmount.of(100000L), Currency.Id.of(strkId));
        accountingService.allocate(strkSponsorAccount.account().id(), ProjectId.of(projectId2), PositiveAmount.of(100000L), Currency.Id.of(strkId));

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

        quoteStorage.save(List.of(
                new Quote(Currency.Id.of(accountingHelper.strk().id()), Currency.Id.of(accountingHelper.usd().id()), BigDecimal.valueOf(strkToUsd1),
                        Instant.now().minus(10, ChronoUnit.DAYS))
        ));

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
        selfEmployedBPAdminRewardId11 =
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

        selfEmployedBPAdminRewardId12 = rewardRepository.findAll().stream()
                .filter(r -> r.projectId().equals(projectId1) && r.recipientId().equals(selfEmployedBPAdminGithubId) && r.amount().longValue() == 55L)
                .findFirst().map(RewardEntity::id).orElse(null);

        userRepository.findByGithubUserId(individualBPAdminGithubId).ifPresent(userEntity -> individualBPAdminId = userEntity.getId());
        userRepository.findByGithubUserId(companyBPAdmin1GithubId).ifPresent(userEntity -> companyBPAdmin1Id = userEntity.getId());
        userRepository.findByGithubUserId(companyBPAdmin2GithubId).ifPresent(userEntity -> companyBPAdmin2Id = userEntity.getId());
        userRepository.findByGithubUserId(companyBPMember1GithubId).ifPresent(userEntity -> companyBPMember1Id = userEntity.getId());
        userRepository.findByGithubUserId(selfEmployedBPAdminGithubId).ifPresent(userEntity -> selfEmployedBPAdminId = userEntity.getId());

        individualBPId = isNull(individualBPAdminId) ? null :
                billingProfileService.getBillingProfilesForUser(UserId.of(individualBPAdminId)).stream().map(s -> s.getId().value()).findFirst().orElse(null);
        companyBPId = isNull(companyBPAdmin1Id) ? null :
                billingProfileService.getBillingProfilesForUser(UserId.of(companyBPAdmin1Id)).stream().map(s -> s.getId().value()).findFirst().orElse(null);
        selfEmployedBPId = isNull(selfEmployedBPAdminId) ? null :
                billingProfileService.getBillingProfilesForUser(UserId.of(selfEmployedBPAdminId)).stream().map(s -> s.getId().value()).findFirst().orElse(null);
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
                        selfEmployedBPAdminRewardId11, "PENDING_SIGNUP"
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
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR"
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
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
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR"
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
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
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR"
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
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
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR"
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
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
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR"
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));
    }

    @Test
    @Order(35)
    void should_add_billing_profile_on_new_reward_based_on_payout_preferences() {
        // Given
        setUp();
        sendRewardToRecipient(selfEmployedBPAdminGithubId, 55L, projectId1);
        selfEmployedBPAdminRewardId12 = rewardRepository.findAll().stream()
                .filter(r -> r.projectId().equals(projectId1) && r.recipientId().equals(selfEmployedBPAdminGithubId) && r.amount().longValue() == 55L)
                .findFirst().map(RewardEntity::id).orElse(null);

        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));
    }

    @Test
    @Order(40)
    void should_display_reward_statuses_given_payout_info_missing() {
        // Given
        setUp();
        resetAuth0Mock();
        quoteStorage.save(List.of(
                new Quote(Currency.Id.of(accountingHelper.strk().id()), Currency.Id.of(accountingHelper.usd().id()), BigDecimal.valueOf(strkToUsd2),
                        Instant.now().minus(5, ChronoUnit.DAYS))
        ));

        // To avoid to stub all the Sumsub flow ...
        final UUID kycId = billingProfileService.getBillingProfile(BillingProfile.Id.of(individualBPId), UserId.of(individualBPAdminId),
                GithubUserId.of(individualBPAdminGithubId)).getKyc().getId();
        billingProfileStoragePort.saveKyc(Kyc.builder()
                .id(kycId)
                .externalApplicantId(faker.rickAndMorty().character())
                .address(faker.address().fullAddress())
                .usCitizen(false)
                .birthdate(new Date())
                .country(Country.fromIso3("ARG"))
                .idDocumentType(Kyc.IdDocumentTypeEnum.ID_CARD)
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .ownerId(UserId.of(individualBPAdminId))
                .billingProfileId(BillingProfile.Id.of(individualBPId))
                .status(VerificationStatus.VERIFIED)
                .build());
        billingProfileStoragePort.updateBillingProfileStatus(BillingProfile.Id.of(individualBPId), VerificationStatus.VERIFIED);
        accountingObserver.onBillingProfileUpdated(new BillingProfileVerificationUpdated(kycId, VerificationType.KYC, VerificationStatus.VERIFIED, null,
                UserId.of(individualBPId), null, faker.rickAndMorty().character(), null));

        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.empty())
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
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        // When
        final UUID companyKybId = billingProfileService.getBillingProfile(BillingProfile.Id.of(companyBPId), UserId.of(companyBPAdmin1Id),
                GithubUserId.of(companyBPAdmin1GithubId)).getKyb().getId();
        billingProfileStoragePort.saveKyb(Kyb.builder()
                .billingProfileId(BillingProfile.Id.of(companyBPId))
                .registrationNumber(faker.idNumber().valid())
                .status(VerificationStatus.VERIFIED)
                .ownerId(UserId.of(companyBPAdmin1Id))
                .country(Country.fromIso3("FRA"))
                .address(faker.address().fullAddress())
                .externalApplicantId(faker.rickAndMorty().character())
                .registrationDate(new Date())
                .id(companyKybId)
                .subjectToEuropeVAT(false)
                .usEntity(false)
                .name(faker.name().fullName())
                .build());
        billingProfileStoragePort.updateBillingProfileStatus(BillingProfile.Id.of(companyBPId), VerificationStatus.VERIFIED);
        accountingObserver.onBillingProfileUpdated(new BillingProfileVerificationUpdated(companyKybId, VerificationType.KYB, VerificationStatus.VERIFIED, null,
                UserId.of(companyBPAdmin1Id), null, faker.rickAndMorty().character(), null));
        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
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
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_VERIFICATION")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        // When
        final UUID selfEmployedKybId =
                billingProfileService.getBillingProfile(BillingProfile.Id.of(selfEmployedBPId), UserId.of(selfEmployedBPAdminId),
                        GithubUserId.of(selfEmployedBPAdminGithubId)).getKyb().getId();
        billingProfileStoragePort.saveKyb(Kyb.builder()
                .billingProfileId(BillingProfile.Id.of(selfEmployedBPId))
                .registrationNumber(faker.idNumber().valid())
                .status(VerificationStatus.VERIFIED)
                .ownerId(UserId.of(selfEmployedBPAdminId))
                .country(Country.fromIso3("FRA"))
                .address(faker.address().fullAddress())
                .externalApplicantId(faker.rickAndMorty().character())
                .registrationDate(new Date())
                .id(selfEmployedKybId)
                .subjectToEuropeVAT(false)
                .usEntity(false)
                .name(faker.name().fullName())
                .build());
        billingProfileStoragePort.updateBillingProfileStatus(BillingProfile.Id.of(selfEmployedBPId), VerificationStatus.VERIFIED);
        accountingObserver.onBillingProfileUpdated(new BillingProfileVerificationUpdated(selfEmployedKybId, VerificationType.KYB, VerificationStatus.VERIFIED
                , null,
                UserId.of(selfEmployedBPId), null, faker.rickAndMorty().character(), null));

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
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
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));
    }

    @Test
    void should_not_display_transaction_details_given_a_billing_profile_member() {
        // TODO X: ne pas remonter les receipt details pour un BP member
    }


    @Test
    @Order(50)
    void should_display_reward_statuses_given_pending_request() {
        // Given
        setUp();

        // When
        billingProfileService.updatePayoutInfo(BillingProfile.Id.of(individualBPId), UserId.of(individualBPAdminId), PayoutInfo.builder()
                .ethWallet(new WalletLocator(new Name("ilysse.eth")))
                .build());

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
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
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "100")))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(individualBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pendingRequestCount").isEqualTo(1);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(individualBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(1)
                .jsonPath("$.billingProfiles[0].invoiceableRewardCount").isEqualTo(1);

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(individualBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(individualBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.invoiceableRewardCount").isEqualTo(1)
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(5000)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(0);

        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(individualBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(individualBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.length()").isEqualTo(1)
                .jsonPath("$.rewards[0].id").isEqualTo(individualBPAdminRewardId1.toString());

        // When
        billingProfileService.updatePayoutInfo(BillingProfile.Id.of(companyBPId), UserId.of(companyBPAdmin2Id), PayoutInfo.builder()
                .ethWallet(new WalletLocator(new Name("pierre.eth")))
                .build());

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
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
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PAYOUT_INFO_MISSING")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "100")))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(companyBPAdmin1GithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pendingRequestCount").isEqualTo(3);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(companyBPAdmin1GithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(1)
                .jsonPath("$.billingProfiles[0].invoiceableRewardCount").isEqualTo(3);

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(companyBPAdmin1GithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.invoiceableRewardCount").isEqualTo(3);

        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(companyBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(companyBPAdmin1GithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.length()").isEqualTo(3)
                .jsonPath("$.rewards[?(@.id == '%s')].id".formatted(companyBPAdmin1RewardId1)).isEqualTo(companyBPAdmin1RewardId1.toString())
                .jsonPath("$.rewards[?(@.id == '%s')].id".formatted(companyBPAdmin2RewardId1)).isEqualTo(companyBPAdmin2RewardId1.toString())
                .jsonPath("$.rewards[?(@.id == '%s')].id".formatted(companyBPMember1RewardId1)).isEqualTo(companyBPMember1RewardId1.toString())
        ;


        // When
        billingProfileService.updatePayoutInfo(BillingProfile.Id.of(selfEmployedBPId), UserId.of(selfEmployedBPAdminId), PayoutInfo.builder()
                .ethWallet(new WalletLocator(new Name("pixelfact.eth")))
                .build());

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
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
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "100")))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(selfEmployedBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pendingRequestCount").isEqualTo(2);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(selfEmployedBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(1)
                .jsonPath("$.billingProfiles[0].invoiceableRewardCount").isEqualTo(2);

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(selfEmployedBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(selfEmployedBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.invoiceableRewardCount").isEqualTo(2);

        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(selfEmployedBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(selfEmployedBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.length()").isEqualTo(2)
                .jsonPath("$.rewards[?(@.id == '%s')].id".formatted(selfEmployedBPAdminRewardId11)).isEqualTo(selfEmployedBPAdminRewardId11.toString())
                .jsonPath("$.rewards[?(@.id == '%s')].id".formatted(selfEmployedBPAdminRewardId12)).isEqualTo(selfEmployedBPAdminRewardId12.toString());

    }

    @Test
    @Order(60)
    void should_display_reward_statuses_given_processing() {
        // Given
        setUp();

        // When
        final Invoice individualInvoice = billingProfileService.previewInvoice(UserId.of(individualBPAdminId), BillingProfile.Id.of(individualBPId),
                List.of(RewardId.of(individualBPAdminRewardId1)));
        billingProfileService.uploadGeneratedInvoice(UserId.of(individualBPAdminId), BillingProfile.Id.of(individualBPId), individualInvoice.id(),
                new ByteArrayInputStream(faker.address().fullAddress().getBytes()));

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PROCESSING",
                        companyBPAdmin1RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPAdmin2RewardId1, "PENDING_CONTRIBUTOR",
                        companyBPMember1RewardId1, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PROCESSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
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
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        // When
        final Invoice companyInvoice = billingProfileService.previewInvoice(UserId.of(companyBPAdmin2Id), BillingProfile.Id.of(companyBPId),
                List.of(RewardId.of(companyBPAdmin1RewardId1), RewardId.of(companyBPMember1RewardId1), RewardId.of(companyBPAdmin2RewardId1)));
        billingProfileService.updateInvoiceMandateAcceptanceDate(UserId.of(companyBPAdmin2Id), BillingProfile.Id.of(companyBPId));
        billingProfileService.uploadGeneratedInvoice(UserId.of(companyBPAdmin2Id), BillingProfile.Id.of(companyBPId), companyInvoice.id(),
                new ByteArrayInputStream(faker.address().fullAddress().getBytes()));

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PROCESSING",
                        companyBPAdmin1RewardId1, "PROCESSING",
                        companyBPAdmin2RewardId1, "PROCESSING",
                        companyBPMember1RewardId1, "PROCESSING",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PROCESSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        // When
        final Invoice selfEmployedInvoiceToReject = billingProfileService.previewInvoice(UserId.of(selfEmployedBPAdminId),
                BillingProfile.Id.of(selfEmployedBPId),
                List.of(RewardId.of(selfEmployedBPAdminRewardId11)));
        billingProfileService.uploadExternalInvoice(UserId.of(selfEmployedBPAdminId), BillingProfile.Id.of(selfEmployedBPId), selfEmployedInvoiceToReject.id(),
                faker.name().firstName(), new ByteArrayInputStream(faker.address().fullAddress().getBytes()));

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PROCESSING",
                        companyBPAdmin1RewardId1, "PROCESSING",
                        companyBPAdmin2RewardId1, "PROCESSING",
                        companyBPMember1RewardId1, "PROCESSING",
                        selfEmployedBPAdminRewardId11, "PROCESSING",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PROCESSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PROCESSING")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        // When
        invoiceService.update(selfEmployedInvoiceToReject.id(), Invoice.Status.REJECTED, faker.chuckNorris().fact());

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PROCESSING",
                        companyBPAdmin1RewardId1, "PROCESSING",
                        companyBPAdmin2RewardId1, "PROCESSING",
                        companyBPMember1RewardId1, "PROCESSING",
                        selfEmployedBPAdminRewardId11, "PENDING_CONTRIBUTOR",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PROCESSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        // When
        final Invoice selfEmployedInvoiceToAccept = billingProfileService.previewInvoice(UserId.of(selfEmployedBPAdminId),
                BillingProfile.Id.of(selfEmployedBPId),
                List.of(RewardId.of(selfEmployedBPAdminRewardId11)));
        billingProfileService.uploadExternalInvoice(UserId.of(selfEmployedBPAdminId), BillingProfile.Id.of(selfEmployedBPId), selfEmployedInvoiceToAccept.id(),
                faker.name().firstName(), new ByteArrayInputStream(faker.address().fullAddress().getBytes()));
        invoiceService.update(selfEmployedInvoiceToAccept.id(), Invoice.Status.APPROVED, null);

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PROCESSING",
                        companyBPAdmin1RewardId1, "PROCESSING",
                        companyBPAdmin2RewardId1, "PROCESSING",
                        companyBPMember1RewardId1, "PROCESSING",
                        selfEmployedBPAdminRewardId11, "PROCESSING",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PROCESSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PROCESSING")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(individualBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(individualBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.invoiceableRewardCount").isEqualTo(0)
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(5000)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(34);
    }

    @Test
    @Order(61)
    void should_remain_in_status_processing_after_we_change_payout_preferences() {
        // Given
        setUp();

        final var anotherBillingProfile = billingProfileService.createSelfEmployedBillingProfile(UserId.of(individualBPAdminId)
                , faker.lordOfTheRings().character(), null);

        // When
        updatePayoutPreferences(individualBPAdminGithubId, anotherBillingProfile.id(), projectId1);

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "PROCESSING",
                        companyBPAdmin1RewardId1, "PROCESSING",
                        companyBPAdmin2RewardId1, "PROCESSING",
                        companyBPMember1RewardId1, "PROCESSING",
                        selfEmployedBPAdminRewardId11, "PROCESSING",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("PROCESSING")
                                        .rewardedAmount(10L)
                                        .pendingAmount(10L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(30L)
                                        .pendingAmount(30L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(20L)
                                        .pendingAmount(20L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("PROCESSING")
                                        .rewardedAmount(40L)
                                        .pendingAmount(40L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("PROCESSING")
                                        .rewardedAmount(50L)
                                        .pendingAmount(50L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(55L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(500L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        // Tear down
        updatePayoutPreferences(individualBPAdminGithubId, BillingProfile.Id.of(individualBPId), projectId1);
    }

    @Autowired
    CurrencyFacadePort currencyFacadePort;
    @Autowired
    UserFacadePort userFacadePort;
    @Autowired
    SponsorStoragePort sponsorStoragePort;
    @Autowired
    AccountingRewardStoragePort accountingRewardStoragePort;
    @Autowired
    MailNotificationPort mailNotificationPort;
    @Autowired
    InvoiceStoragePort invoiceStoragePort;

    @Test
    @Order(70)
    void should_display_reward_statuses_given_completed() {
        // Given
        setUp();
        resetAuth0Mock();
        quoteStorage.save(List.of(
                new Quote(Currency.Id.of(accountingHelper.strk().id()), Currency.Id.of(accountingHelper.usd().id()), BigDecimal.valueOf(strkToUsd3),
                        Instant.now())
        ));
        final var backofficeAccountingManagementRestApi = new BackofficeAccountingManagementRestApi(
                accountingService,
                rewardService,
                userFacadePort,
                new onlydust.com.marketplace.accounting.domain.service.RewardService(accountingRewardStoragePort, mailNotificationPort, accountingService,
                        sponsorStoragePort),
                new PaymentService(accountingRewardStoragePort, invoiceStoragePort, accountingService),
                billingProfileService);
        backofficeAccountingManagementRestApi.payReward(individualBPAdminRewardId1,
                new PayRewardRequest().network(TransactionNetwork.ETHEREUM).recipientAccount("0xa11c0edBa8924280Df7f258B370371bD985C8B0B").reference(
                        "0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156"));
        backofficeAccountingManagementRestApi.payReward(companyBPAdmin1RewardId1,
                new PayRewardRequest().network(TransactionNetwork.ETHEREUM).recipientAccount("0x12919307dd40A2f5DEf83cE598A5b042a83D23E0").reference(
                        "0xccd727561376b00898b2a163d66d08d16b0ec2590ada079f5353568c04460523"));
        backofficeAccountingManagementRestApi.payReward(companyBPAdmin2RewardId1,
                new PayRewardRequest().network(TransactionNetwork.ETHEREUM).recipientAccount("0x12919307dd40A2f5DEf83cE598A5b042a83D23E0").reference(
                        "0xccd727561376b00898b2a163d66d08d16b0ec2590ada079f5353568c04460523"));
        backofficeAccountingManagementRestApi.payReward(companyBPMember1RewardId1,
                new PayRewardRequest().network(TransactionNetwork.ETHEREUM).recipientAccount("0x12919307dd40A2f5DEf83cE598A5b042a83D23E0").reference(
                        "0xf27dc84666851b0b140c5190eb706d80821966f857b5fbf510f283ad6c8d283e"));
        backofficeAccountingManagementRestApi.payReward(selfEmployedBPAdminRewardId11,
                new PayRewardRequest().network(TransactionNetwork.ETHEREUM).recipientAccount("0x5b8a9A23c729010FB1DBFcC0e8e5eCeB4CC81cD9").reference(
                        "0x9da1b9ded266895e097a18378789c3f09bb0a541d8c17c0d9c7a95bb3072ffa0"));

        // Then
        assertGetProjectRewardsStatusOnProject(
                projectId1,
                Map.of(
                        individualBPAdminRewardId1, "COMPLETE",
                        companyBPAdmin1RewardId1, "COMPLETE",
                        companyBPAdmin2RewardId1, "COMPLETE",
                        companyBPMember1RewardId1, "COMPLETE",
                        selfEmployedBPAdminRewardId11, "COMPLETE",
                        selfEmployedBPAdminRewardId12, "PENDING_CONTRIBUTOR"
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
                                        .status("COMPLETE")
                                        .rewardedAmount(10L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(individualBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(100L)
                                        .pendingAmount(100L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("COMPLETE")
                                        .rewardedAmount(40L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("COMPLETE")
                                        .rewardedAmount(20L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(200L)
                                        .pendingAmount(200L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("COMPLETE")
                                        .rewardedAmount(30L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPAdmin2GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("COMPLETE")
                                        .rewardedAmount(40L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId1)
                                        .status("COMPLETE")
                                        .rewardedAmount(30L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin2RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(300L)
                                        .pendingAmount(300L)
                                        .usdConversionRate(Optional.empty())
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPAdmin1RewardId1)
                                        .status("COMPLETE")
                                        .rewardedAmount(20L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(companyBPMember1GithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId1)
                                        .status("COMPLETE")
                                        .rewardedAmount(40L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(companyBPMember1RewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(400L)
                                        .pendingAmount(400L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build(),
                MyRewardDatum.builder()
                        .githubUserId(selfEmployedBPAdminGithubId)
                        .rewardData(List.of(
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId11)
                                        .status("COMPLETE")
                                        .rewardedAmount(50L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId12)
                                        .status("PENDING_REQUEST")
                                        .rewardedAmount(55L)
                                        .pendingAmount(0L)
                                        .usdConversionRate(Optional.of(strkToUsd2))
                                        .build(),
                                RewardDatum.builder()
                                        .rewardId(selfEmployedBPAdminRewardId2)
                                        .status("PENDING_BILLING_PROFILE")
                                        .rewardedAmount(500L)
                                        .pendingAmount(555L)
                                        .usdConversionRate(Optional.empty())
                                        .build()
                        ))
                        .build()
        ));

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(individualBPId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateUser(individualBPAdminGithubId).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.invoiceableRewardCount").isEqualTo(0)
                .jsonPath("$.currentYearPaymentLimit").isEqualTo(5000)
                .jsonPath("$.currentYearPaymentAmount").isEqualTo(34);
    }

    // TODO X: tester les cas particuliers des snails : PAYMENT_BLOCKED et LOCKED

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
                    .jsonPath("$.rewards.length()").isEqualTo(myRewardDatum.rewardData.size());

            long rewardedAmount = 0;
            long pendingAmount = 0;
            BigDecimal usdEquivalent = BigDecimal.ZERO;

            for (RewardDatum rewardDatum : myRewardDatum.rewardData()) {
                final var rewardAmountAsList = new MutableObject<List<Integer>>();
                final var rewardUsdEquivalentAsList = new MutableObject<List<Double>>();
                bodyContentSpec
                        .jsonPath("$.rewards[?(@.id == '%s')].status".formatted(rewardDatum.rewardId.toString()))
                        .isEqualTo(rewardDatum.status)
                        .jsonPath("$.rewards[?(@.id == '%s')].amount.total".formatted(rewardDatum.rewardId.toString()))
                        .value(rewardAmountAsList::setValue)
                        .jsonPath("$.rewards[?(@.id == '%s')].amount.dollarsEquivalent".formatted(rewardDatum.rewardId.toString()))
                        .value(rewardUsdEquivalentAsList::setValue);
                assertThat(rewardAmountAsList.getValue().get(0).longValue()).isEqualTo(rewardDatum.rewardedAmount);
                assertThat(rewardUsdEquivalentAsList.getValue().get(0)).isEqualTo(rewardDatum.usdEquivalent().map(BigDecimal::doubleValue).orElse(null));

                if (rewardDatum.status().equals("COMPLETE")) {
                    bodyContentSpec.jsonPath("$.rewards[?(@.id == '%s')].processedAt".formatted(rewardDatum.rewardId.toString())).isNotEmpty();
                } else {
                    bodyContentSpec.jsonPath("$.rewards[?(@.id == '%s')].processedAt".formatted(rewardDatum.rewardId.toString())).isEqualTo(null);
                }

                rewardedAmount += rewardDatum.rewardedAmount;
                pendingAmount += rewardDatum.pendingAmount;
                usdEquivalent = usdEquivalent.add(rewardDatum.usdEquivalent().orElse(BigDecimal.ZERO));

                final var rewardAmount = new MutableObject<Long>();
                final var rewardUsdEquivalent = new MutableObject<BigDecimal>();
                client.get()
                        .uri(getApiURI(ME_REWARD.formatted(rewardDatum.rewardId.toString())))
                        .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                        // Then
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody()
                        .jsonPath("$.status").isEqualTo(rewardDatum.status)
                        .jsonPath("$.amount").value(rewardAmount::setValue, Long.class)
                        .jsonPath("$.dollarsEquivalent").value(rewardUsdEquivalent::setValue, BigDecimal.class);
                assertThat(rewardAmount.getValue()).isEqualTo(rewardDatum.rewardedAmount);
                assertThat(rewardUsdEquivalent.getValue()).isEqualTo(rewardDatum.usdEquivalent().orElse(null));
            }


            final var totalAmount = new MutableObject<Long>();
            final var totalUsdEquivalent = new MutableObject<BigDecimal>();
            bodyContentSpec
                    .jsonPath("$.rewardedAmount.amount").value(totalAmount::setValue, Long.class)
                    .jsonPath("$.rewardedAmount.usdEquivalent").value(totalUsdEquivalent::setValue, BigDecimal.class)
                    .jsonPath("$.pendingAmount.amount").isEqualTo(pendingAmount);

            assertThat(totalAmount.getValue()).isEqualTo(rewardedAmount);
            assertThat(totalUsdEquivalent.getValue()).isEqualTo(usdEquivalent);
        }
    }

    @Builder
    private record MyRewardDatum(@NonNull Long githubUserId, @NonNull List<RewardDatum> rewardData) {
    }

    @Builder
    private record RewardDatum(@NonNull UUID rewardId, @NonNull String status, @NonNull Long rewardedAmount, @NonNull Long pendingAmount,
                               @NonNull Optional<Double> usdConversionRate) {

        Optional<BigDecimal> usdEquivalent() {
            return usdConversionRate.map(rate -> BigDecimal.valueOf(rewardedAmount).multiply(BigDecimal.valueOf(rate)));
        }
    }

    private void assertGetProjectRewardsStatusOnProject(final UUID projectId, final Map<UUID, String> rewardStatusMapToId) {
        // When
        final WebTestClient.BodyContentSpec json = client.get()
                .uri(getApiURI(PROJECTS_REWARDS.formatted(projectId), Map.of("pageIndex", "0", "pageSize", "20")))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticatePierre().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(rewardStatusMapToId.size());

        for (Map.Entry<UUID, String> rewardIdStatus : rewardStatusMapToId.entrySet()) {
            json.jsonPath("$.rewards[?(@.id == '%s')].status"
                    .formatted(rewardIdStatus.getKey().toString())).isEqualTo(rewardIdStatus.getValue());
            if (rewardIdStatus.getValue().equals("COMPLETE")) {
                json.jsonPath("$.rewards[?(@.id == '%s')].processedAt".formatted(rewardIdStatus.getKey().toString())).isNotEmpty();
            } else {
                json.jsonPath("$.rewards[?(@.id == '%s')].processedAt".formatted(rewardIdStatus.getKey().toString())).isEqualTo(null);
            }
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
        if (rewardId.equals(selfEmployedBPAdminRewardId11)) {
            return GET_PROJECT_11_SELF_EMPLOYED_REWARD_JSON_RESPONSE;
        }
        if (rewardId.equals(selfEmployedBPAdminRewardId12)) {
            return GET_PROJECT_12_SELF_EMPLOYED_REWARD_JSON_RESPONSE;
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
              
              "unlockDate": null,
              "from": {
                "githubUserId": 16590657,
                "login": "PierreOucif",
                "htmlUrl": "https://github.com/PierreOucif",
                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "isRegistered": true
              },
              "to": {
                "githubUserId": 1,
                "login": "mmaderic",
                "htmlUrl": "https://github.com/mmaderic",
                "avatarUrl": "https://avatars.githubusercontent.com/u/39437117?v=4"
              },
              
              "project": {
                "slug": "super-project-1",
                "name": "Super Project 1",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              }
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
              
              "unlockDate": null,
              "from": {
                "githubUserId": 16590657,
                "login": "PierreOucif",
                "htmlUrl": "https://github.com/PierreOucif",
                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "isRegistered": true
              },
              "to": {
                "githubUserId": 1,
                "login": "mmaderic",
                "htmlUrl": "https://github.com/mmaderic",
                "avatarUrl": "https://avatars.githubusercontent.com/u/39437117?v=4"
              },
              
              "project": {
                "slug": "super-project-2",
                "name": "Super Project 2",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              }
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
               
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": "https://github.com/PierreOucif",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": true
               },
               "to": {
                 "githubUserId": 2,
                 "login": "jannesblobel",
                 "htmlUrl": "https://github.com/jannesblobel",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/72493222?v=4"
               },
               
               "project": {
                 "slug": "super-project-1",
                 "name": "Super Project 1",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               }
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
               
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": "https://github.com/PierreOucif",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": true
               },
               "to": {
                 "githubUserId": 2,
                 "login": "jannesblobel",
                 "htmlUrl": "https://github.com/jannesblobel",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/72493222?v=4"
               },
               
               "project": {
                 "slug": "super-project-2",
                 "name": "Super Project 2",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               }
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
               
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": "https://github.com/PierreOucif",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": true
               },
               "to": {
                 "githubUserId": 3,
                 "login": "nickdbush",
                 "htmlUrl": "https://github.com/nickdbush",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/10998201?v=4"
               },
               
               "project": {
                 "slug": "super-project-1",
                 "name": "Super Project 1",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               }
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
               
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": "https://github.com/PierreOucif",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": true
               },
               "to": {
                 "githubUserId": 3,
                 "login": "nickdbush",
                 "htmlUrl": "https://github.com/nickdbush",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/10998201?v=4"
               },
               
               "project": {
                 "slug": "super-project-2",
                 "name": "Super Project 2",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               }
             }
            """;

    private static final String GET_PROJECT_11_SELF_EMPLOYED_REWARD_JSON_RESPONSE = """
            {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 50,
               
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": "https://github.com/PierreOucif",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": true
               },
               "to": {
                 "githubUserId": 5,
                 "login": "yanns",
                 "htmlUrl": "https://github.com/yanns",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/51669?v=4"
               },
               
               "project": {
                 "slug": "super-project-1",
                 "name": "Super Project 1",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               }
             }
            """;

    private static final String GET_PROJECT_12_SELF_EMPLOYED_REWARD_JSON_RESPONSE = """
            {
               "currency": {
                 "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                 "code": "STRK",
                 "name": "StarkNet Token",
                 "logoUrl": null,
                 "decimals": 18
               },
               "amount": 55,
               
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": "https://github.com/PierreOucif",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": true
               },
               "to": {
                 "githubUserId": 5,
                 "login": "yanns",
                 "htmlUrl": "https://github.com/yanns",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/51669?v=4"
               },
               
               "project": {
                 "slug": "super-project-1",
                 "name": "Super Project 1",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               }
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
               
               "unlockDate": null,
               "from": {
                 "githubUserId": 16590657,
                 "login": "PierreOucif",
                 "htmlUrl": "https://github.com/PierreOucif",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "isRegistered": true
               },
               "to": {
                 "githubUserId": 5,
                 "login": "yanns",
                 "htmlUrl": "https://github.com/yanns",
                 "avatarUrl": "https://avatars.githubusercontent.com/u/51669?v=4"
               },
               
               "project": {
                 "slug": "super-project-2",
                 "name": "Super Project 2",
                 "shortDescription": "This is a super project",
                 "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                 "visibility": "PUBLIC"
               }
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
              
              "unlockDate": null,
              "from": {
                "githubUserId": 16590657,
                "login": "PierreOucif",
                "htmlUrl": "https://github.com/PierreOucif",
                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "isRegistered": true
              },
              "to": {
                "githubUserId": 4,
                "login": "acomminos",
                "htmlUrl": "https://github.com/acomminos",
                "avatarUrl": "https://avatars.githubusercontent.com/u/628035?v=4"
              },
              
              "project": {
                "slug": "super-project-1",
                "name": "Super Project 1",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              }
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
              "unlockDate": null,
              "from": {
                "githubUserId": 16590657,
                "login": "PierreOucif",
                "htmlUrl": "https://github.com/PierreOucif",
                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "isRegistered": true
              },
              "to": {
                "githubUserId": 4,
                "login": "acomminos",
                "htmlUrl": "https://github.com/acomminos",
                "avatarUrl": "https://avatars.githubusercontent.com/u/628035?v=4"
              },
              
              "project": {
                "slug": "super-project-2",
                "name": "Super Project 2",
                "shortDescription": "This is a super project",
                "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "visibility": "PUBLIC"
              }
            }
            """;
}

