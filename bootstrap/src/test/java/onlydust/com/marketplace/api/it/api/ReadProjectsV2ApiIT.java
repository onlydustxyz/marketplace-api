package onlydust.com.marketplace.api.it.api;

import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.javafaker.Faker;

import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
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

    static final Faker faker = new Faker();
    static final AtomicBoolean setupDone = new AtomicBoolean();
    static Project project;
    static List<Ecosystem> ecosystems;
    static List<ProjectCategory> categories;
    static List<NamedLink> moreInfos;
    static UserAuthHelper.AuthenticatedUser projectLead;
    static GithubRepo repo1;
    static GithubRepo repo2;

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
            .mapToObj(i -> projectHelper.createCategory(faker.lorem().word()))
            .peek(c -> projectHelper.addCategory(projectId, c.id()))
            .toList();

        moreInfos = IntStream.range(0,2)
            .mapToObj(i -> projectHelper.createMoreInfoLink())
            .peek(m -> projectHelper.addMoreInfo(projectId, m))
            .toList();

        final var pastHackathonLabel = "od-past";
        hackathonHelper.createHackathon(Hackathon.Status.PUBLISHED, List.of(pastHackathonLabel), List.of(projectId), ZonedDateTime.now().minusDays(2), ZonedDateTime.now().minusDays(1));

        final var liveHackathonLabel = "od-live";
        hackathonHelper.createHackathon(Hackathon.Status.PUBLISHED, List.of(liveHackathonLabel), List.of(projectId), ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(1));

        final var draftHackathonLabel = "od-draft";
        hackathonHelper.createHackathon(Hackathon.Status.DRAFT, List.of(draftHackathonLabel), List.of(projectId), ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2));

        final var upcomingHackathonLabel = "od-upcoming";
        hackathonHelper.createHackathon(Hackathon.Status.PUBLISHED, List.of(upcomingHackathonLabel), List.of(projectId), ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2));

        final var contributor1 = userAuthHelper.create();
        final var contributor2 = userAuthHelper.create();

        at(ZonedDateTime.now().minusDays(1), () -> {
            // merged pull requests
            githubHelper.createPullRequest(repo1, contributor1, List.of("java"));
            githubHelper.createPullRequest(repo2, contributor1, List.of("js", "ts"));
            
            // available issue
            githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1);

            // assigned issue
            final var assignedIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1); 
            githubHelper.assignIssueToContributor(assignedIssueId, contributor2.user().getGithubUserId());
            
            // closed issue
            githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), CurrentDateProvider.now().plusHours(2), "COMPLETED", contributor1);

            // available good first issue
            final var goodFirstIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(goodFirstIssueId, "good first issue", CurrentDateProvider.now());

            // assigned good first issue
            final var assignedGoodFirstIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(assignedGoodFirstIssueId, "good first issue", CurrentDateProvider.now());
            githubHelper.assignIssueToContributor(assignedGoodFirstIssueId, contributor1.user().getGithubUserId());

            // closed good first issue
            final var closedGoodFirstIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), CurrentDateProvider.now().plusHours(2), "COMPLETED", contributor1);
            githubHelper.addLabelToIssue(closedGoodFirstIssueId, "good first issue", CurrentDateProvider.now());

            // hackathon open issues
            final var pastHackathonIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(pastHackathonIssueId, pastHackathonLabel, CurrentDateProvider.now());
            
            final var liveHackathonIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(liveHackathonIssueId, liveHackathonLabel, CurrentDateProvider.now());

            final var draftHackathonIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(draftHackathonIssueId, draftHackathonLabel, CurrentDateProvider.now());

            final var upcomingHackathonIssueId = githubHelper.createIssue(repo1.getId(), CurrentDateProvider.now(), null, "OPEN", contributor1);
            githubHelper.addLabelToIssue(upcomingHackathonIssueId, upcomingHackathonLabel, CurrentDateProvider.now());
        });

        githubHelper.addRepoLanguage(repo1.getId(), "Java", 100L);
        githubHelper.addRepoLanguage(repo2.getId(), "JavaScript", 10L);
        githubHelper.addRepoLanguage(repo2.getId(), "TypeScript", 50L);

        databaseHelper.executeInTransaction(() -> {
            biProjectGlobalDataRepository.refreshByProject(projectId);
            biProjectContributionsDataRepository.refreshByProject(projectId);
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
        assertThat(response.getContributorCount()).isEqualTo(2);
        assertThat(response.getStarCount()).isEqualTo(repo1.getStarsCount() + repo2.getStarsCount());
        assertThat(response.getForkCount()).isEqualTo(repo1.getForksCount() + repo2.getForksCount());
        assertThat(response.getAvailableIssueCount()).isEqualTo(5);
        assertThat(response.getGoodFirstIssueCount()).isEqualTo(1);
        assertThat(response.getMergedPrCount()).isEqualTo(2);

        assertThat(response.getCategories())
            .extracting(ProjectCategoryResponse::getName)
            .containsExactlyElementsOf(categories.stream().map(ProjectCategory::name).sorted().toList());
        
        assertThat(response.getLanguages())
            .extracting(LanguageWithPercentageResponse::getName)
            .containsExactly("Java", "TypeScript", "JavaScript");
            
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
}
