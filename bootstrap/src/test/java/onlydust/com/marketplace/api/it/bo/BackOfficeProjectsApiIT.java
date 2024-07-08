package onlydust.com.marketplace.api.it.bo;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import onlydust.com.backoffice.api.contract.model.RewardContributorRequest;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeProjectRestApi;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class BackOfficeProjectsApiIT extends AbstractMarketplaceBackOfficeApiIT {

    private UserAuthHelper.AuthenticatedBackofficeUser pierre;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER,
                BackofficeUser.Role.BO_FINANCIAL_ADMIN));
    }

    @Autowired
    BackofficeProjectRestApi.OnlydustBotProperties onlydustBotProperties;
    @Autowired
    AccountingService accountingService;

    private void allocateBudgetToProject() {
        final UUID sponsorId = UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d");
        final UUID projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");
        final SponsorAccountStatement strkSponsorAccount = accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(CurrencyHelper.STRK.value()), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));

        accountingService.allocate(strkSponsorAccount.account().id(), ProjectId.of(projectId), PositiveAmount.of(100000L),
                Currency.Id.of(CurrencyHelper.STRK.value()));
    }

    @Autowired
    EntityManagerFactory entityManagerFactory;

    private void indexRecipient() {
        final EntityManager em = entityManagerFactory.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.createNativeQuery("INSERT INTO indexer_exp.github_accounts (id, login, type, html_url, avatar_url, name) VALUES (212, 'aterrien', 'USER', " +
                             "'https://github.com/aterrien', 'https://avatars.githubusercontent.com/u/212?v=4', 'Fake user');").executeUpdate();
        em.flush();
        tx.commit();
        em.close();
    }

    @Test
    void should_reward_contributors() {
        // Given
        allocateBudgetToProject();
        indexRecipient();
        onlydustBotProperties.setProjectLeadId(UUID.fromString("45e98bf6-25c2-4edf-94da-e340daba8964"));
        final RewardContributorRequest rewardContributorRequest = new RewardContributorRequest();
        rewardContributorRequest.setAmount(BigDecimal.valueOf(111.95));
        rewardContributorRequest.setReason(faker.rickAndMorty().location());
        rewardContributorRequest.setCurrencyCode("STRK");
        rewardContributorRequest.setRepositoryName("bretzel-app");
        final String recipientGithubLogin = "aterrien";
        rewardContributorRequest.setRecipientGithubLogin(recipientGithubLogin);

        githubWireMockServer.stubFor(get(urlEqualTo("/search/users?per_page=5&q=%s".formatted(recipientGithubLogin)))
                .willReturn(okJson("""
                        {
                          "total_count": 1,
                          "incomplete_results": false,
                          "items": [
                            {
                              "login": "aterrien",
                              "id": 212,
                              "node_id": "MDQ6VXNlcjMxMjIw",
                              "avatar_url": "https://avatars.githubusercontent.com/u/31220?v=4",
                              "gravatar_id": "",
                              "url": "https://api.github.com/users/aterrien",
                              "html_url": "https://github.com/aterrien",
                              "followers_url": "https://api.github.com/users/aterrien/followers",
                              "following_url": "https://api.github.com/users/aterrien/following{/other_user}",
                              "gists_url": "https://api.github.com/users/aterrien/gists{/gist_id}",
                              "starred_url": "https://api.github.com/users/aterrien/starred{/owner}{/repo}",
                              "subscriptions_url": "https://api.github.com/users/aterrien/subscriptions",
                              "organizations_url": "https://api.github.com/users/aterrien/orgs",
                              "repos_url": "https://api.github.com/users/aterrien/repos",
                              "events_url": "https://api.github.com/users/aterrien/events{/privacy}",
                              "received_events_url": "https://api.github.com/users/aterrien/received_events",
                              "type": "User",
                              "site_admin": false,
                              "score": 1.0
                            }
                          ]
                        }
                        """)));

        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/212"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        dustyBotApiWireMockServer.stubFor(post(urlEqualTo("/repos/gregcha/bretzel-app/issues"))
                .willReturn(okJson(CREATE_ISSUE_RESPONSE_JSON)));
        dustyBotApiWireMockServer.stubFor(post(urlEqualTo("/repos/gregcha/bretzel-app/issues/25"))
                .withRequestBody(equalToJson("""
                        {
                            "state": "closed"
                        }
                        """))
                .willReturn(okJson(String.format(CLOSE_ISSUE_RESPONSE_JSON, faker.rickAndMorty().character()))));

        // When
        client.post()
                .uri(getApiURI(PROJECT_REWARDS.formatted("bretzel")))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(rewardContributorRequest))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    private static final String CLOSE_ISSUE_RESPONSE_JSON = """
            {
              "url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25",
              "repository_url": "https://api.github.com/repos/gregcha/bretzel-app",
              "labels_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/labels{/name}",
              "comments_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/comments",
              "events_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/events",
              "html_url": "https://github.com/gregcha/bretzel-app/issues/25",
              "id": 1840630179,
              "node_id": "I_kwDOJ4YlT85ttcmj",
              "number": 25,
              "title": "title",
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
                "url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/reactions",
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
              "timeline_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/timeline",
              "performed_via_github_app": null,
              "state_reason": "completed"
            }""";

    private static final String CREATE_ISSUE_RESPONSE_JSON = """
            {
                                  "url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25",
                                  "repository_url": "https://api.github.com/repos/gregcha/bretzel-app",
                                  "labels_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/labels{/name}",
                                  "comments_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/comments",
                                  "events_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/events",
                                  "html_url": "https://github.com/gregcha/bretzel-app/issues/25",
                                  "id": 1840630179,
                                  "node_id": "I_kwDOJ4YlT85ttcmj",
                                  "number": 25,
                                  "title": "title",
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
                                    "url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/reactions",
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
                                  "timeline_url": "https://api.github.com/repos/gregcha/bretzel-app/issues/25/timeline",
                                  "performed_via_github_app": null,
                                  "state_reason": null
                                }""";
}
