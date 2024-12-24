package onlydust.com.marketplace.api.it.api;

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.BigDecimalComparator.BIG_DECIMAL_COMPARATOR;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.javafaker.Faker;

import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiContributorGlobalDataRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiProjectContributionsDataRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiProjectGlobalDataRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.project.domain.model.*;


@TagProject
public class ReadProjectsV2ApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    BiProjectGlobalDataRepository biProjectGlobalDataRepository;
    @Autowired
    BiProjectContributionsDataRepository biProjectContributionsDataRepository;
    @Autowired
    BiContributorGlobalDataRepository biContributorGlobalDataRepository;

    static final Faker faker = new Faker();
    static final AtomicBoolean setupDone = new AtomicBoolean();
    static Project project;
    static List<Ecosystem> ecosystems;
    static List<ProjectCategory> categories;
    static List<NamedLink> moreInfos;
    static UserAuthHelper.AuthenticatedUser projectLead;
    static GithubRepo repo1;
    static GithubRepo repo2;

    static List<GithubIssue> availableIssues = new ArrayList<>();

    static Long pastHackathonLabelId;
    static Long liveHackathonLabelId;
    static Long draftHackathonLabelId;
    static Long upcomingHackathonLabelId;
    static Long goodFirstIssueLabelId;

    static UserAuthHelper.AuthenticatedUser contributor1;
    static UserAuthHelper.AuthenticatedUser contributor2;
    static UserAuthHelper.AuthenticatedUser contributor3;

    @BeforeEach 
    void setUp() {
        if(setupDone.compareAndExchange(false, true)) return;

        projectLead = userAuthHelper.create();
        final var projectId = projectHelper.create(projectLead).getLeft();

        ecosystems = IntStream.range(0,2)
            .mapToObj(i -> ecosystemHelper.create(faker.internet().slug()))
            .peek(e -> projectHelper.addEcosystem(projectId, EcosystemId.of(e.id())))
            .toList();

        project = projectHelper.get(projectId);
        repo1 = githubHelper.createRepo(projectId);
        repo2 = githubHelper.createRepo(projectId);

        categories = IntStream.range(0,2)
            .mapToObj(i -> projectHelper.createCategory(faker.lorem().word() + "-" + i))
            .peek(c -> projectHelper.addCategory(projectId, c.id()))
            .toList();

        moreInfos = IntStream.range(0,2)
            .mapToObj(i -> projectHelper.createMoreInfoLink())
            .peek(m -> projectHelper.addMoreInfo(projectId, m))
            .toList();

        pastHackathonLabelId = githubHelper.createLabel("od-past");
        hackathonHelper.createHackathon(Hackathon.Status.PUBLISHED, List.of("od-past"), List.of(projectId), ZonedDateTime.now().minusDays(2), ZonedDateTime.now().minusDays(1));

        liveHackathonLabelId = githubHelper.createLabel("od-live");
        hackathonHelper.createHackathon(Hackathon.Status.PUBLISHED, List.of("od-live"), List.of(projectId), ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(1));

        draftHackathonLabelId = githubHelper.createLabel("od-draft");
        hackathonHelper.createHackathon(Hackathon.Status.DRAFT, List.of("od-draft"), List.of(projectId), ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2));

        upcomingHackathonLabelId = githubHelper.createLabel("od-upcoming");
        hackathonHelper.createHackathon(Hackathon.Status.PUBLISHED, List.of("od-upcoming"), List.of(projectId), ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2));

        goodFirstIssueLabelId = githubHelper.createLabel("good first issue");

        contributor1 = userAuthHelper.create();
        contributor2 = userAuthHelper.create();
        contributor3 = userAuthHelper.create();

        databaseHelper.executeInTransaction(() -> {
            biContributorGlobalDataRepository.refresh(contributor1.user().getGithubUserId());
            biContributorGlobalDataRepository.refresh(contributor2.user().getGithubUserId());
            biContributorGlobalDataRepository.refresh(contributor3.user().getGithubUserId());
        });

        at(ZonedDateTime.now().minusWeeks(2), () -> {
            // merged pull requests
            githubHelper.createPullRequest(repo1, contributor3, List.of("rs"));
            
            // available issue
            availableIssues.add(githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(1), null, "OPEN", contributor1));
            availableIssues.add(githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(2), null, "OPEN", contributor1));

            // available good first issue
            final var goodFirstIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(3), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(goodFirstIssue.id(), goodFirstIssueLabelId, CurrentDateProvider.now());
            availableIssues.add(goodFirstIssue);

            // hackathon open issues
            final var pastHackathonIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(4), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(pastHackathonIssue.id(), pastHackathonLabelId, CurrentDateProvider.now());
            githubHelper.addLabelToIssue(pastHackathonIssue.id(), goodFirstIssueLabelId, CurrentDateProvider.now());
            availableIssues.add(pastHackathonIssue);

            final var upcomingHackathonIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(5), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(upcomingHackathonIssue.id(), upcomingHackathonLabelId, CurrentDateProvider.now());
        });

        at(ZonedDateTime.now().minusDays(1), () -> {
            // merged pull requests
            githubHelper.createPullRequest(repo1, contributor1, List.of("java"));
            githubHelper.createPullRequest(repo2, contributor1, List.of("js", "ts"));
            
            // available issue
            availableIssues.add(githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(1), null, "OPEN", contributor1));

            // assigned issue
            final var assignedIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(2), null, "OPEN", contributor1); 
            githubHelper.assignIssueToContributor(assignedIssue.id(), contributor2.user().getGithubUserId());
            
            // closed issue
            githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(3), CurrentDateProvider.now().plusHours(2), "COMPLETED", contributor1);

            // available good first issue
            final var goodFirstIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(4), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(goodFirstIssue.id(), goodFirstIssueLabelId, CurrentDateProvider.now());
            availableIssues.add(goodFirstIssue);

            // assigned good first issue
            final var assignedGoodFirstIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(5), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(assignedGoodFirstIssue.id(), goodFirstIssueLabelId, CurrentDateProvider.now());
            githubHelper.assignIssueToContributor(assignedGoodFirstIssue.id(), contributor1.user().getGithubUserId());

            // closed good first issue
            final var closedGoodFirstIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(6), CurrentDateProvider.now().plusHours(2), "COMPLETED", contributor1);
            githubHelper.addLabelToIssue(closedGoodFirstIssue.id(), goodFirstIssueLabelId, CurrentDateProvider.now());

            // hackathon open issues
            final var pastHackathonIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(7), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(pastHackathonIssue.id(), pastHackathonLabelId, CurrentDateProvider.now());
            availableIssues.add(pastHackathonIssue);

            final var liveHackathonIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(8), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(liveHackathonIssue.id(), liveHackathonLabelId, CurrentDateProvider.now());
            availableIssues.add(liveHackathonIssue);

            final var draftHackathonIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(9), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(draftHackathonIssue.id(), draftHackathonLabelId, CurrentDateProvider.now());
            githubHelper.addLabelToIssue(draftHackathonIssue.id(), goodFirstIssueLabelId, CurrentDateProvider.now());
            availableIssues.add(draftHackathonIssue);

            final var upcomingHackathonIssue = githubHelper.createIssue(repo1, CurrentDateProvider.now().plusSeconds(10), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(upcomingHackathonIssue.id(), upcomingHackathonLabelId, CurrentDateProvider.now());
        });

        accountingHelper.saveQuote(new Quote(CurrencyHelper.STRK, CurrencyHelper.USD, BigDecimal.valueOf(0.55), Instant.now()));

        final var sponsor = sponsorHelper.create();
        accountingHelper.createSponsorAccount(sponsor.id(), 1000, CurrencyHelper.USD);
        accountingHelper.createSponsorAccount(sponsor.id(), 1000, CurrencyHelper.STRK);

        final var program = programHelper.create(sponsor.id());
        accountingHelper.allocate(sponsor.id(), program.id(), 1000, CurrencyHelper.USD);
        accountingHelper.allocate(sponsor.id(), program.id(), 1000, CurrencyHelper.STRK);

        accountingHelper.grant(program.id(), projectId, 1000, CurrencyHelper.USD);
        accountingHelper.grant(program.id(), projectId, 1000, CurrencyHelper.STRK);

        rewardHelper.create(projectId, projectLead, contributor2.githubUserId(), 120, CurrencyHelper.STRK);
        rewardHelper.create(projectId, projectLead, contributor2.githubUserId(), 100, CurrencyHelper.USD);

        githubHelper.addRepoLanguage(repo1.getId(), "Java", 100L);
        githubHelper.addRepoLanguage(repo2.getId(), "JavaScript", 10L);
        githubHelper.addRepoLanguage(repo2.getId(), "TypeScript", 50L);
        githubHelper.addRepoLanguage(repo2.getId(), "Rust", 30L);

        databaseHelper.executeInTransaction(() -> {
            biProjectGlobalDataRepository.refreshByProject(projectId);
            biProjectContributionsDataRepository.refreshByProject(projectId);
            userRepository.refreshUsersRanksAndStats();
            biContributorGlobalDataRepository.refresh(contributor1.user().getGithubUserId());
            biContributorGlobalDataRepository.refresh(contributor2.user().getGithubUserId());
            biContributorGlobalDataRepository.refresh(contributor3.user().getGithubUserId());
        });
    }

    @Test
    public void should_get_project_by_id() {
        should_get_project_by_id_or_slug(project.getId().toString());
    }

    @Test
    public void should_get_project_by_slug() {
        should_get_project_by_id_or_slug(project.getSlug());
    }

    private void should_get_project_by_id_or_slug(String idOrSlug) {
        // When
        final var response = client.get()
                .uri(getApiURI(PROJECTS_V2_GET_BY_ID_OR_SLUG.formatted(idOrSlug)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProjectResponseV2.class)
                .returnResult().getResponseBody();
                
        assertThat(response.getId()).isEqualTo(project.getId().value());
        assertThat(response.getSlug()).isEqualTo(project.getSlug());
        assertThat(response.getName()).isEqualTo(project.getName());
        assertThat(response.getLogoUrl()).isEqualTo(project.getLogoUrl());
        assertThat(response.getShortDescription()).isEqualTo(project.getShortDescription());
        assertThat(response.getContributorCount()).isEqualTo(3);
        assertThat(response.getStarCount()).isEqualTo(repo1.getStarsCount() + repo2.getStarsCount());
        assertThat(response.getForkCount()).isEqualTo(repo1.getForksCount() + repo2.getForksCount());

        assertThat(response.getAvailableIssueCount()).isEqualTo(9);
        assertThat(response.getCurrentWeekAvailableIssueCount()).isEqualTo(5);

        assertThat(response.getGoodFirstIssueCount()).isEqualTo(4);
        
        assertThat(response.getMergedPrCount()).isEqualTo(3);
        assertThat(response.getCurrentWeekMergedPrCount()).isEqualTo(2);

        assertThat(response.getCategories())
            .extracting(ProjectCategoryResponse::getName)
            .containsExactlyElementsOf(categories.stream().map(ProjectCategory::name).sorted().toList());
        
        assertThat(response.getLanguages())
            .extracting(LanguageWithPercentageResponse::getName)
            .containsExactly("Java", "TypeScript", "Rust", "JavaScript");
            
        assertThat(response.getEcosystems())
            .extracting(EcosystemLinkResponse::getName)
            .containsExactlyElementsOf(ecosystems.stream().map(Ecosystem::name).sorted().toList());
        
        assertThat(response.getLeads())
            .extracting(GithubUserResponse::getLogin)
            .containsExactly(projectLead.user().getGithubLogin());

        assertThat(response.getMoreInfos())
            .extracting(SimpleLink::getUrl)
            .containsExactlyElementsOf(moreInfos.stream().map(NamedLink::getUrl).toList());
    }

    @Test
    public void should_get_project_available_issues_by_id() {
        should_get_project_available_issues(project.getId().toString());
    }

    @Test
    public void should_filter_issues_by_labels_by_id() {
        assertThat(should_filter_issues_by_labels(project.getId().toString(), "good first issue", "od-past")
            .getLabels())
            .usingRecursiveFieldByFieldElementComparatorOnFields("name", "count")
            .containsExactly(
                new GithubLabelWithCountResponse().name("good first issue").count(1),
                new GithubLabelWithCountResponse().name("od-past").count(1)
            );
    }

    @Test
    public void should_get_project_available_issues_by_slug() {
        should_get_project_available_issues(project.getSlug());
    }

    private void should_get_project_available_issues(String idOrSlug) {
        // When
        final var response = get_project_available_issues_by_id_or_slug(idOrSlug);
        final var issues = response.getIssues();
        final var labels = response.getLabels();

        assertThat(issues)
            .hasSize(availableIssues.size())
            .isSortedAccordingTo(comparing(GithubIssuePageItemResponse::getCreatedAt).reversed());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getId)
            .containsExactlyInAnyOrderElementsOf(availableIssues.stream().map(GithubIssue::id).map(GithubIssue.Id::value).toList());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getTitle)
            .containsExactlyInAnyOrderElementsOf(availableIssues.stream().map(GithubIssue::title).toList());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getNumber)
            .containsExactlyInAnyOrderElementsOf(availableIssues.stream().map(GithubIssue::number).toList());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getStatus)
            .containsOnly(GithubIssueStatus.OPEN);

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getHtmlUrl)
            .containsExactlyInAnyOrderElementsOf(availableIssues.stream().map(GithubIssue::htmlUrl).toList());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getRepo)
            .extracting(ShortGithubRepoResponse::getId)
            .containsExactlyInAnyOrderElementsOf(availableIssues.stream().map(GithubIssue::repoId).toList());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getAuthor)
            .extracting(ContributorResponse::getLogin)
            .containsExactlyInAnyOrderElementsOf(availableIssues.stream().map(GithubIssue::authorLogin).toList());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getCreatedAt)
            .allMatch(d -> d.isAfter(ZonedDateTime.now().minusWeeks(2).minusDays(1)));

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getClosedAt)
            .containsOnlyNulls();

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getBody)
            .containsExactlyInAnyOrderElementsOf(availableIssues.stream().map(GithubIssue::description).toList());

        assertThat(issues)
            .flatExtracting(GithubIssuePageItemResponse::getLabels)
            .extracting(GithubLabel::getName)
            .containsOnly("good first issue", "od-past", "od-live", "od-draft");

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getApplicants)
            .allMatch(applicants -> applicants.isEmpty());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getAssignees)
            .allMatch(assignees -> assignees.isEmpty());

        assertThat(issues)
            .extracting(GithubIssuePageItemResponse::getCommentCount)
            .allMatch(c -> c > 0);

        assertThat(labels)
            .hasSize(4)
            .isSortedAccordingTo(comparing(GithubLabelWithCountResponse::getCount).reversed());

        assertThat(labels)
            .extracting(GithubLabelWithCountResponse::getName)
            .containsExactlyInAnyOrder("good first issue", "od-past", "od-live", "od-draft");

        assertThat(labels)
            .extracting(GithubLabelWithCountResponse::getCount)
            .containsExactly(4, 2, 1, 1);
    }

    private GithubIssuePageWithLabelsResponse should_filter_issues_by_labels(String idOrSlug, String... labels) {
        final var response = get_project_available_issues_by_id_or_slug(idOrSlug, labels);
        final var issues = response.getIssues();
        final var allLabels = response.getLabels();

        assertThat(issues)
            .isNotEmpty()
            .extracting(GithubIssuePageItemResponse::getLabels)
            .map(l -> l.stream().map(GithubLabel::getName).toList())
            .allMatch(issueLabels -> Arrays.stream(labels).allMatch(issueLabels::contains));

        assertThat(allLabels)
            .isNotEmpty()
            .isSortedAccordingTo(comparing(GithubLabelWithCountResponse::getCount).reversed())
            .extracting(GithubLabelWithCountResponse::getName)
            .contains(labels);

        return response;
    }

    private GithubIssuePageWithLabelsResponse get_project_available_issues_by_id_or_slug(String idOrSlug, String... labels) {
        // Given
        final var params = new HashMap<String, String>();
        params.put("pageSize", "10");
        if (labels.length > 0) 
            params.put("githubLabels", String.join(",", labels));

        // When
        return client.get()
                .uri(getApiURI(PROJECTS_GET_AVAILABLE_ISSUES_BY_ID_OR_SLUG.formatted(idOrSlug), params))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(GithubIssuePageWithLabelsResponse.class)
                .returnResult().getResponseBody();
    }

    @Test
    public void should_get_project_contributors_by_id() {
        should_get_project_contributors(project.getId().toString());
    }

    @Test
    public void should_get_project_contributors_by_slug() {
        should_get_project_contributors(project.getSlug());
    }

    @Test
    public void should_filter_contributors_by_login() {
        // Given
        final var search = contributor1.user().getGithubLogin().substring(0, 3);

        // When
        final var contributors = get_project_contributors(project.getId().toString(), search.toUpperCase())
            .getContributors();

        assertThat(contributors)
            .isNotEmpty()
            .extracting(ContributorPageItemResponseV2::getLogin)
            .allMatch(login -> login.toLowerCase().contains(search.toLowerCase()));
    }

    private void should_get_project_contributors(String idOrSlug) {
        // When
        final var contributors = get_project_contributors(idOrSlug, null)
            .getContributors();

        assertThat(contributors)
            .hasSize(3)
            .isSortedAccordingTo(comparing(ContributorPageItemResponseV2::getGlobalRank))
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withComparatorForType(BIG_DECIMAL_COMPARATOR, BigDecimal.class)
                .build())
            .containsExactlyInAnyOrder(
                new ContributorPageItemResponseV2()
                    .githubUserId(contributor1.user().getGithubUserId())
                    .login(contributor1.user().getGithubLogin())
                    .avatarUrl(contributor1.user().getGithubAvatarUrl())
                    .isRegistered(true)
                    .id(contributor1.userId().value())
                    .globalRank(80)
                    .globalRankPercentile(BigDecimal.valueOf(0.0035872194877784266))
                    .globalRankCategory(UserRankCategory.A)
                    .mergedPullRequestCount(2)
                    .rewardCount(0)
                    .totalEarnedUsdAmount(BigDecimal.valueOf(0)),

                new ContributorPageItemResponseV2()
                    .githubUserId(contributor2.user().getGithubUserId())
                    .login(contributor2.user().getGithubLogin())
                    .avatarUrl(contributor2.user().getGithubAvatarUrl())
                    .isRegistered(true)
                    .id(contributor2.userId().value())
                    .globalRank(149)
                    .globalRankPercentile(BigDecimal.valueOf(0.006215066321848669))
                    .globalRankCategory(UserRankCategory.A)
                    .mergedPullRequestCount(0)
                    .rewardCount(2)
                    .totalEarnedUsdAmount(BigDecimal.valueOf(166)),

                new ContributorPageItemResponseV2()
                    .githubUserId(contributor3.user().getGithubUserId())
                    .login(contributor3.user().getGithubLogin())
                    .avatarUrl(contributor3.user().getGithubAvatarUrl())
                    .isRegistered(true)
                    .id(contributor3.userId().value())
                    .globalRank(89)
                    .globalRankPercentile(BigDecimal.valueOf(0.006048218903812463))
                    .globalRankCategory(UserRankCategory.A)
                    .mergedPullRequestCount(1)
                    .rewardCount(0)
                    .totalEarnedUsdAmount(BigDecimal.valueOf(0))
            );
    }

    private ContributorsPageResponseV2 get_project_contributors(String idOrSlug, String login) {
        final var params = new HashMap<String, String>();
        params.put("pageSize", "10");
        if (login != null) 
            params.put("login", login);

        // When
        return client.get()
                .uri(getApiURI(PROJECTS_V2_GET_CONTRIBUTORS_BY_ID_OR_SLUG.formatted(idOrSlug), params))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ContributorsPageResponseV2.class)
                .returnResult().getResponseBody();
    }
}
