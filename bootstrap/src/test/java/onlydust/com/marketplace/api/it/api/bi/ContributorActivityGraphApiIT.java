package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.ContributorActivityGraphResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@TagBI
public class ContributorActivityGraphApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setup() {
        caller = userAuthHelper.authenticateOlivier();
    }


    @Test
    public void should_get_activity_graph() {
        test_activity_graph(666L, Map.of("dataSource", "ALL"), Map.of());
    }

    private void test_activity_graph(Long contributorId, Map<String, String> queryParams, Map<Integer, ExpectedDay> expectedDays) {
        final var response = client.get()
                .uri(getApiURI(BI_CONTRIBUTORS_ACTIVITY_GRAPH.formatted(contributorId), queryParams))
                .header("Authorization", BEARER_PREFIX + caller.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ContributorActivityGraphResponse.class)
                .consumeWith(System.out::println)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getDays()).hasSize(365);

        final var today = ZonedDateTime.now();
        final var days = response.getDays();
        var minusDay = 0;
        for (int i = days.size() - 1; i >= 0; i--) {
            final var day = days.get(i);
            assertThat(day.getDay()).isEqualTo(today.minusDays(minusDay).getDayOfYear());
            assertThat(day.getWeek()).isEqualTo(today.minusDays(minusDay).get(WeekFields.ISO.weekOfWeekBasedYear()));
            assertThat(day.getYear()).isEqualTo(today.minusDays(minusDay).getYear());
            final var expected = expectedDays.get(minusDay);
            if (expected != null) {
                assertThat(day.getIssueCount()).isEqualTo(expected.issueCount);
                assertThat(day.getPullRequestCount()).isEqualTo(expected.pullRequestCount);
                assertThat(day.getCodeReviewCount()).isEqualTo(expected.codeReviewCount);
                assertThat(day.getRewardCount()).isEqualTo(expected.rewardCount);
            } else {
                assertThat(day.getIssueCount()).isZero();
                assertThat(day.getPullRequestCount()).isZero();
                assertThat(day.getCodeReviewCount()).isZero();
                assertThat(day.getRewardCount()).isZero();
            }
            minusDay++;
        }
    }

    private record ExpectedDay(int issueCount, int pullRequestCount, int codeReviewCount, int rewardCount) {
    }

    @Nested
    class ActiveContributors {
        private static final AtomicBoolean setupDone = new AtomicBoolean();
        private static UserAuthHelper.AuthenticatedUser antho;
        private static ProjectId onlyDust;

        @AfterAll
        @SneakyThrows
        static void restore() {
            restoreIndexerDump();
        }

        @BeforeEach
        synchronized void setup() {
            if (setupDone.compareAndExchange(false, true)) return;

            final var today = ZonedDateTime.now().withHour(15).withMinute(0).withSecond(0).withNano(0);

            antho = userAuthHelper.create("antho-foo");
            final var pierre = userAuthHelper.create("pierre-foo");
            final var mehdi = userAuthHelper.create("mehdi-foo");

            final var starknetFoundation = sponsorHelper.create();
            accountingHelper.createSponsorAccount(starknetFoundation.id(), 10_000, STRK);

            ProgramId explorationTeam = programHelper.create(starknetFoundation.id(), "Starkware Exploration Team", caller).id();
            accountingHelper.allocate(starknetFoundation.id(), explorationTeam, 10_000, STRK);

            onlyDust = projectHelper.create(pierre, "OnlyDust").getLeft();
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(explorationTeam, onlyDust, 100, STRK));
            final var marketplace_api = githubHelper.createRepo(onlyDust);

            final var otherProject = projectHelper.create(pierre, "Other").getLeft();
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(explorationTeam, otherProject, 100, STRK));
            final var other_repo = githubHelper.createRepo(otherProject);

            final var not_in_onlydust_repo = githubHelper.createRepo();

            final var prUUID = at(today.minusDays(1), () -> githubHelper.createPullRequest(marketplace_api, antho));
            at(today.minusDays(1), () -> githubHelper.createPullRequest(other_repo, antho));
            at(today.minusDays(1), () -> githubHelper.createPullRequest(not_in_onlydust_repo, antho));
            at(today.minusDays(1), () -> githubHelper.createPullRequest(marketplace_api, mehdi));

            at(today.minusDays(2), () -> {
                final var issueId = githubHelper.createIssue(marketplace_api, antho);
                githubHelper.assignIssueToContributor(issueId, antho.githubUserId().value());
            });
            at(today.minusDays(2), () -> githubHelper.createCodeReview(marketplace_api, prUUID, antho));
            at(today.minusDays(2), () -> githubHelper.createPullRequest(marketplace_api, antho));
            at(today.minusDays(2), () -> githubHelper.createPullRequest(other_repo, antho));
            at(today.minusDays(2), () -> githubHelper.createPullRequest(not_in_onlydust_repo, antho));
            at(today.minusDays(2), () -> githubHelper.createPullRequest(marketplace_api, mehdi));

            at(today.minusDays(3), () -> githubHelper.createPullRequest(marketplace_api, antho));
            at(today.minusDays(3), () -> githubHelper.createPullRequest(marketplace_api, antho));
            at(today.minusDays(3), () -> githubHelper.createPullRequest(marketplace_api, mehdi));

            currencyHelper.setQuote("2020-12-31T00:00:00Z", STRK, USD, BigDecimal.valueOf(0.5));
            currencyHelper.setQuote("2020-12-31T00:00:00Z", ETH, USD, BigDecimal.valueOf(2));

            at(today.minusDays(2), () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 1, STRK));
            at(today.minusDays(3), () -> rewardHelper.create(otherProject, pierre, antho.githubUserId(), 2, STRK));
            at(today.minusDays(3), () -> rewardHelper.create(onlyDust, pierre, mehdi.githubUserId(), 3, STRK));
            at(today.minusDays(4), () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 4, STRK));

            projectFacadePort.refreshStats();
        }

        @Test
        public void should_get_activity_graph() {
            test_activity_graph(antho.githubUserId().value(), Map.of("dataSource", "ALL"), Map.of(
                    1, new ExpectedDay(0, 3, 0, 0),
                    2, new ExpectedDay(1, 3, 1, 1),
                    3, new ExpectedDay(0, 2, 0, 1),
                    4, new ExpectedDay(0, 0, 0, 1)
            ));
            test_activity_graph(antho.githubUserId().value(), Map.of("dataSource", "ONLYDUST"), Map.of(
                    1, new ExpectedDay(0, 2, 0, 0),
                    2, new ExpectedDay(1, 2, 1, 1),
                    3, new ExpectedDay(0, 2, 0, 1),
                    4, new ExpectedDay(0, 0, 0, 1)
            ));
            test_activity_graph(antho.githubUserId().value(), Map.of("dataSource", "ONLYDUST", "dataSourceProjectId", onlyDust.toString()), Map.of(
                    1, new ExpectedDay(0, 1, 0, 0),
                    2, new ExpectedDay(1, 1, 1, 1),
                    3, new ExpectedDay(0, 2, 0, 0),
                    4, new ExpectedDay(0, 0, 0, 1)
            ));
        }
    }
}
