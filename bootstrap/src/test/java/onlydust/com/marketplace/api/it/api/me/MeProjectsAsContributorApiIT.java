package onlydust.com.marketplace.api.it.api.me;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.ETH;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.STRK;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.USD;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;

@TagMe
public class MeProjectsAsContributorApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;
    @Autowired
    BillingProfileFacadePort billingProfileService;

    @Nested
    class ActiveContributors {
        private static final AtomicBoolean setupDone = new AtomicBoolean();
        private static ProgramId nethermind;
        private static UserAuthHelper.AuthenticatedUser lead;
        private static UserAuthHelper.AuthenticatedUser antho;
        private static UserAuthHelper.AuthenticatedUser mehdi;
        private static ProjectCategory defi;
        private static ProjectCategory gaming;
        private static ProjectId onlyDustId;

        @AfterAll
        @SneakyThrows
        static void restore() {
            restoreIndexerDump();
        }

        @BeforeEach
        synchronized void setup() {
            if (setupDone.compareAndExchange(false, true)) return;

            currencyHelper.setQuote("2020-12-31T00:00:00Z", STRK, USD, BigDecimal.valueOf(0.5));
            currencyHelper.setQuote("2020-12-31T00:00:00Z", ETH, USD, BigDecimal.valueOf(2));

            lead = userAuthHelper.create("olivier");
            antho = userAuthHelper.create("antho");
            mehdi = userAuthHelper.create("mehdi");

            defi = projectHelper.createCategory("DeFi");
            gaming = projectHelper.createCategory("Gaming");

            final var starknetFoundation = sponsorHelper.create("The Starknet Foundation");
            accountingHelper.createSponsorAccount(starknetFoundation.id(), 10_000, STRK);

            nethermind = programHelper.create(starknetFoundation.id(), "Nethermind").id();
            accountingHelper.allocate(starknetFoundation.id(), nethermind, 3_000, STRK);

            final var onlyDust = projectHelper.create(lead, "OnlyDust");
            onlyDustId = onlyDust.getLeft();
            projectHelper.addCategory(onlyDustId, defi.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDustId, 100, STRK));

            final var marketplace_api = githubHelper.createRepo("marketplace_api", onlyDustId);

            final var bridge = projectHelper.create(lead, "Bridge").getLeft();
            projectHelper.addCategory(bridge, gaming.id());

            final var bridge_frontend = githubHelper.createRepo("bridge", bridge);


            final Long goodFirstIssue = githubHelper.createIssue(marketplace_api, ZonedDateTime.now(), null, "OPEN", lead).id().value();
            githubHelper.addLabelToIssue(goodFirstIssue, "good-first-issue", ZonedDateTime.now());

            at("2021-01-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho, List.of("java")));
            final var issueId = at("2021-01-01T00:00:03Z", () -> githubHelper.createIssue(bridge_frontend, mehdi));
            githubHelper.assignIssueToContributor(issueId, mehdi.user().getGithubUserId());

            at("2021-01-01T00:00:04Z", () -> githubHelper.createPullRequest(bridge_frontend, mehdi, List.of("ts")));

            final var prId = at("2021-01-02T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho, List.of("rs")));
            at("2021-01-03T00:00:03Z", () -> githubHelper.createCodeReview(bridge_frontend, prId, mehdi));
            at("2021-01-04T00:00:04Z", () -> githubHelper.createPullRequest(bridge_frontend, mehdi, List.of("ts")));

            at("2021-02-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho));
            at("2021-02-03T00:00:03Z", () -> githubHelper.createPullRequest(bridge_frontend, mehdi));
            at("2021-02-05T00:00:04Z", () -> githubHelper.createPullRequest(bridge_frontend, mehdi));

            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDustId, lead, mehdi.githubUserId(), 1, STRK));

            final IndividualBillingProfile anthoBillingProfile = billingProfileService.createIndividualBillingProfile(antho.userId(),
                    "Antho's BP", Set.of(onlyDustId));
            final IndividualBillingProfile mehdiBillingProfile = billingProfileService.createIndividualBillingProfile(mehdi.userId(),
                    "Mehdi's BP", Set.of(bridge));
            final SelfEmployedBillingProfile selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(mehdi.userId(),
                    "Mehdi's Inc", Set.of(onlyDustId));

            projectFacadePort.refreshStats();
        }

        @Test
        public void should_get_antho_projects() {
            client.get()
                    .uri(getApiURI(ME_AS_CONTRIBUTOR_PROJECTS, Map.of("pageIndex", "0", "pageSize", "100")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(antho).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.projects[0].slug").value((String s) -> assertThat(s).contains("onlydust"))
                    .jsonPath("$.projects[0].name").value((String s) -> assertThat(s).contains("OnlyDust"))
                    .jsonPath("$.projects[0].goodFirstIssueIds.length()").isEqualTo(1)
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 1,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "projects": [
                                {
                                  "visibility": "PUBLIC",
                                  "languages": [
                                    {
                                      "slug": "java",
                                      "name": "Java"
                                    },
                                    {
                                      "slug": "rust",
                                      "name": "Rust"
                                    }
                                  ],
                                  "leads": [
                                    {
                                      "login": "olivier"
                                    }
                                  ],
                                  "repos": [
                                    {
                                      "name": "marketplace_api"
                                    }
                                  ],
                                  "contributorCount": 1,
                                  "contributionCount": 3,
                                  "rewardedUsdAmount": 0,
                                  "billingProfile": {
                                    "type": "INDIVIDUAL",
                                    "name": "Antho's BP",
                                    "rewardCount": 0,
                                    "invoiceableRewardCount": 0,
                                    "requestableRewardCount": 0,
                                    "invoiceMandateAccepted": false,
                                    "enabled": true,
                                    "pendingInvitationResponse": false,
                                    "role": "ADMIN",
                                    "missingPayoutInfo": false,
                                    "missingVerification": false,
                                    "verificationBlocked": false,
                                    "currentYearPaymentLimit": null,
                                    "currentYearPaymentAmount": 0,
                                    "individualLimitReached": false
                                  }
                                }
                              ]
                            }
                            """);
        }

        @Test
        public void should_get_mehdi_projects() {
            client.get()
                    .uri(getApiURI(ME_AS_CONTRIBUTOR_PROJECTS, Map.of("pageIndex", "0", "pageSize", "100")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(mehdi).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.projects[0].slug").value((String s) -> assertThat(s).contains("bridge"))
                    .jsonPath("$.projects[0].name").value((String s) -> assertThat(s).contains("Bridge"))
                    .jsonPath("$.projects[0].goodFirstIssueIds.length()").isEqualTo(0)
                    .jsonPath("$.projects[1].slug").value((String s) -> assertThat(s).contains("onlydust"))
                    .jsonPath("$.projects[1].name").value((String s) -> assertThat(s).contains("OnlyDust"))
                    .jsonPath("$.projects[1].goodFirstIssueIds.length()").isEqualTo(1)
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 2,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "projects": [
                                {
                                  "visibility": "PUBLIC",
                                  "languages": [
                                    {
                                      "slug": "typescript",
                                      "name": "TypeScript"
                                    }
                                  ],
                                  "leads": [
                                    {
                                      "login": "olivier"
                                    }
                                  ],
                                  "repos": [
                                    {
                                      "name": "bridge"
                                    }
                                  ],
                                  "goodFirstIssueIds": [],
                                  "contributorCount": 1,
                                  "contributionCount": 6,
                                  "rewardedUsdAmount": 0,
                                  "billingProfile": {
                                    "type": "INDIVIDUAL",
                                    "name": "Mehdi's BP",
                                    "rewardCount": 0,
                                    "invoiceableRewardCount": 0,
                                    "requestableRewardCount": 0,
                                    "invoiceMandateAccepted": false,
                                    "enabled": true,
                                    "pendingInvitationResponse": false,
                                    "role": "ADMIN",
                                    "missingPayoutInfo": false,
                                    "missingVerification": false,
                                    "verificationBlocked": false,
                                    "currentYearPaymentLimit": null,
                                    "currentYearPaymentAmount": 0,
                                    "individualLimitReached": false
                                  }
                                },
                                {
                                  "visibility": "PUBLIC",
                                  "languages": [
                                    {
                                      "slug": "java",
                                      "name": "Java"
                                    },
                                    {
                                      "slug": "rust",
                                      "name": "Rust"
                                    }
                                  ],
                                  "leads": [
                                    {
                                      "login": "olivier"
                                    }
                                  ],
                                  "repos": [
                                    {
                                      "name": "marketplace_api"
                                    }
                                  ],
                                  "contributorCount": 1,
                                  "contributionCount": 0,
                                  "rewardedUsdAmount": 0.5,
                                  "billingProfile": {
                                    "type": "SELF_EMPLOYED",
                                    "name": "Mehdi's Inc",
                                    "rewardCount": 1,
                                    "invoiceableRewardCount": 0,
                                    "requestableRewardCount": 0,
                                    "invoiceMandateAccepted": false,
                                    "enabled": true,
                                    "pendingInvitationResponse": false,
                                    "role": "ADMIN",
                                    "missingPayoutInfo": false,
                                    "missingVerification": true,
                                    "verificationBlocked": false,
                                    "currentYearPaymentLimit": null,
                                    "currentYearPaymentAmount": 0,
                                    "individualLimitReached": false
                                  }
                                }
                              ]
                            }
                            """);
        }

    }
}
