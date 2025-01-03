package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.MoneyResponse;
import onlydust.com.backoffice.api.contract.model.ProjectWithBudgetResponse;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectMoreInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectTagViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramStatPerCurrencyPerProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity.getGithubAppInstallationPermissionsUpdateUrl;
import static onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper.map;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;
import static org.hibernate.Hibernate.size;


@Entity
@NoArgsConstructor
@Table(name = "projects", schema = "public")
@Immutable
@Accessors(fluent = true)
@Getter
public class ProjectReadEntity {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "name")
    String name;
    @Column(name = "created_at")
    Instant createdAt;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "long_description")
    String longDescription;
    @Column(name = "telegram_link")
    String telegramLink;
    @Column(name = "logo_url")
    String logoUrl;
    @Column(name = "hiring")
    Boolean hiring;
    @Column(name = "rank")
    Integer rank;
    @Column(name = "slug")
    String slug;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibility visibility;
    @Column(name = "reward_ignore_pull_requests_by_default")
    Boolean ignorePullRequests;
    @Column(name = "reward_ignore_issues_by_default")
    Boolean ignoreIssues;
    @Column(name = "reward_ignore_code_reviews_by_default")
    Boolean ignoreCodeReviews;
    @Column(name = "reward_ignore_contributions_before_date_by_default")
    Date ignoreContributionsBefore;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_github_repos",
            schema = "public",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "github_repo_id")
    )
    Set<GithubRepoViewEntity> repos;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    Set<ProjectMoreInfoViewEntity> moreInfos;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    Set<ProjectTagViewEntity> tags;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "projects_project_categories",
            joinColumns = @JoinColumn(name = "projectId"),
            inverseJoinColumns = @JoinColumn(name = "projectCategoryId")
    )
    Set<ProjectCategoryReadEntity> categories;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "project_languages",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    Set<LanguageReadEntity> languages;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "project_leads",
            joinColumns = @JoinColumn(name = "projectId"),
            inverseJoinColumns = @JoinColumn(name = "userId", referencedColumnName = "userId")
    )
    Set<AllUserReadEntity> leads;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "m_active_programs_projects",
            joinColumns = @JoinColumn(name = "projectId"),
            inverseJoinColumns = @JoinColumn(name = "programId")
    )
    Set<ProgramReadEntity> programs;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    Set<ProjectsGoodFirstIssuesReadEntity> goodFirstIssues;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    @NonNull
    Set<ProjectCategorySuggestionReadEntity> categorySuggestions;

    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY)
    @NonNull
    @Getter(AccessLevel.NONE)
    Set<ProjectContributionStatReadEntity> contributionStats;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    Set<ProjectContributorLabelReadEntity> contributorLabels;

    public Optional<ProjectContributionStatReadEntity> contributionStats() {
        return contributionStats.stream().findFirst();
    }

    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY)
    @NonNull
    @Getter(AccessLevel.NONE)
    Set<ProjectRewardStatReadEntity> rewardStats;

    public Optional<ProjectRewardStatReadEntity> rewardStats() {
        return rewardStats.stream().findFirst();
    }

    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY)
    @NonNull
    Set<ProjectStatPerCurrencyReadEntity> globalStatsPerCurrency;

    @ManyToMany
    @NonNull
    @JoinTable(
            name = "program_stats_per_currency_per_project",
            schema = "bi",
            joinColumns = @JoinColumn(name = "projectId"),
            inverseJoinColumns = @JoinColumn(name = "programId")
    )
    Set<ProgramReadEntity> grantingPrograms;

    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY)
    @NonNull
    Set<ProgramStatPerCurrencyPerProjectReadEntity> perProgramStatsPerCurrency;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "projects_pending_contributors",
            joinColumns = @JoinColumn(name = "projectId"),
            inverseJoinColumns = @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId")
    )
    Set<AllUserReadEntity> pendingContributors;

    public List<GithubOrganizationResponse> organizations(final boolean includeAllAvailableRepos) {
        final var organizationEntities = new HashMap<Long, GithubAccountViewEntity>();
        repos().forEach(repo -> organizationEntities.put(repo.getOwner().id(), repo.getOwner()));

        final var repoIdsIncludedInProject = repos().stream()
                .filter(GithubRepoViewEntity::isPublic)
                .map(GithubRepoViewEntity::getId)
                .collect(Collectors.toSet());

        return organizationEntities.values().stream()
                .map(entity -> new GithubOrganizationResponse()
                        .githubUserId(entity.id())
                        .login(entity.login())
                        .avatarUrl(entity.avatarUrl())
                        .htmlUrl(nonNull(entity.htmlUrl()) ? URI.create(entity.htmlUrl()) : null)
                        .name(entity.name())
                        .installationId(entity.installation() != null ? entity.installation().getId() : null)
                        .installationStatus(entity.installation() == null ? GithubOrganizationInstallationStatus.NOT_INSTALLED :
                                switch (entity.installation().getStatus()) {
                                    case SUSPENDED -> GithubOrganizationInstallationStatus.SUSPENDED;
                                    case MISSING_PERMISSIONS -> GithubOrganizationInstallationStatus.MISSING_PERMISSIONS;
                                    case COMPLETE -> GithubOrganizationInstallationStatus.COMPLETE;
                                })
                        .githubAppInstallationPermissionsUpdateUrl(getGithubAppInstallationPermissionsUpdateUrl(entity.login(), entity.type(),
                                ofNullable(entity.installation())))
                        .repos(entity.repos().stream()
                                .filter(GithubRepoViewEntity::isPublic)
                                .filter(repo -> includeAllAvailableRepos || repoIdsIncludedInProject.contains(repo.getId()))
                                .distinct()
                                .map(repo -> new GithubRepoResponse()
                                        .id(repo.getId())
                                        .owner(repo.getOwner().login())
                                        .name(repo.getName())
                                        .description(repo.getDescription())
                                        .htmlUrl(repo.getHtmlUrl())
                                        .stars(isNull(repo.getStarsCount()) ? null : Math.toIntExact(repo.getStarsCount()))
                                        .forkCount(isNull(repo.getForksCount()) ? null : Math.toIntExact(repo.getForksCount()))
                                        .hasIssues(repo.getHasIssues())
                                        .isIncludedInProject(repoIdsIncludedInProject.contains(repo.getId()))
                                        .isAuthorizedInGithubApp(entity.installation() != null &&
                                                                 entity.installation().getAuthorizedRepos().stream()
                                                                         .anyMatch(installedRepo -> installedRepo.getId().getRepoId().equals(repo.getId()))))
                                .sorted(comparing(GithubRepoResponse::getId))
                                .toList())
                )
                .sorted(comparing(GithubOrganizationResponse::getGithubUserId))
                .toList();
    }

    public ProjectShortResponse toShortResponse() {
        return new ProjectShortResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .slug(slug)
                .shortDescription(shortDescription)
                .visibility(visibility)
                .languages(languages.stream().map(LanguageReadEntity::toDto).toList());
    }

    public ProgramProjectPageItemResponse toProgramProjectPageItemResponse(UUID programId) {
        final var statsPerCurrency = perProgramStatsPerCurrency.stream()
                .filter(s -> s.programId().equals(programId))
                .toList();
        final var totalGranted = map(statsPerCurrency, ProgramStatPerCurrencyPerProjectReadEntity::totalGranted);
        final var totalRewarded = map(statsPerCurrency, ProgramStatPerCurrencyPerProjectReadEntity::totalRewarded);

        return new ProgramProjectPageItemResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .logoUrl(logoUrl)
                .shortDescription(shortDescription)
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .languages(languages.stream().map(LanguageReadEntity::toDto).toList())
                .categories(categories.stream().map(ProjectCategoryReadEntity::toDto).toList())
                .totalAvailable(map(statsPerCurrency, s -> s.totalGranted().subtract(s.totalRewarded())))
                .totalGranted(totalGranted)
                .totalRewarded(totalRewarded)
                .percentUsedBudget(totalGranted.getTotalUsdEquivalent().compareTo(ZERO) == 0 ? null :
                        totalRewarded.getTotalUsdEquivalent()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalGranted.getTotalUsdEquivalent(), 0, RoundingMode.HALF_EVEN))
                .averageRewardUsdAmount(prettyUsd(rewardStats().map(ProjectRewardStatReadEntity::averageRewardUsdAmount).orElse(null)))
                .mergedPrCount(createKpi(contributionStats().map(ProjectContributionStatReadEntity::currentPeriodMergedPrCount).orElse(0),
                        contributionStats().map(ProjectContributionStatReadEntity::lastPeriodMergedPrCount).orElse(0)))
                .newContributorsCount(createKpi(contributionStats().map(ProjectContributionStatReadEntity::currentPeriodNewContributorCount).orElse(0),
                        contributionStats().map(ProjectContributionStatReadEntity::lastPeriodActiveContributorCount).orElse(0)))
                .activeContributorsCount(createKpi(contributionStats().map(ProjectContributionStatReadEntity::currentPeriodActiveContributorCount).orElse(0),
                        contributionStats().map(ProjectContributionStatReadEntity::lastPeriodActiveContributorCount).orElse(0)));
    }

    public ProgramProjectResponse toProgramProjectResponse(final UUID programId) {
        final var statsPerCurrency = perProgramStatsPerCurrency.stream()
                .filter(s -> s.programId().equals(programId))
                .toList();
        final var totalGranted = map(statsPerCurrency, ProgramStatPerCurrencyPerProjectReadEntity::totalGranted);
        final var totalRewarded = map(statsPerCurrency, ProgramStatPerCurrencyPerProjectReadEntity::totalRewarded);
        final DetailedTotalMoney totalAvailable = map(statsPerCurrency, s -> s.totalGranted().subtract(s.totalRewarded()));
        return new ProgramProjectResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .logoUrl(logoUrl)
                .totalAvailable(totalAvailable)
                .totalGranted(totalGranted)
                .totalRewarded(totalRewarded);
    }

    private NumberKpi createKpi(int current, int last) {
        return new NumberKpi()
                .value(current)
                .trend(current < last ? Trend.DOWN : current > last ? Trend.UP : Trend.STABLE);
    }

    public ProjectWithBudgetResponse toBoProjectWithBudgetResponse(UUID programId) {
        return new ProjectWithBudgetResponse()
                .id(id)
                .name(name)
                .slug(slug)
                .logoUrl(logoUrl)
                .remainingBudgets(perProgramStatsPerCurrency.stream()
                        .filter(s -> s.programId().equals(programId))
                        .map(s -> new MoneyResponse()
                                .currency(s.currency().toBoShortResponse())
                                .amount(s.totalAvailable()))
                        .toList());
    }

    public ProjectLinkResponse toLinkResponse() {
        return new ProjectLinkResponse()
                .id(id)
                .name(name)
                .slug(slug)
                .logoUrl(logoUrl);
    }

    public MyProjectsAsMaintainerPageItemResponse toMyProjectsPageItemResponse() {
        return new MyProjectsAsMaintainerPageItemResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .logoUrl(logoUrl)
                .shortDescription(shortDescription)
                .visibility(visibility)
                .languages(languages.stream().map(LanguageReadEntity::toDto).toList())
                .leads(leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .contributorCount(size(pendingContributors))
                .totalAvailable(DetailedTotalMoneyMapper.map(globalStatsPerCurrency, ProjectStatPerCurrencyReadEntity::totalAvailable))
                .totalGranted(DetailedTotalMoneyMapper.map(globalStatsPerCurrency, ProjectStatPerCurrencyReadEntity::totalGranted))
                .totalRewarded(DetailedTotalMoneyMapper.map(globalStatsPerCurrency, ProjectStatPerCurrencyReadEntity::totalRewarded))
                ;
    }

    public ProjectFinancialResponse toFinancialResponse() {
        return new ProjectFinancialResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .totalAvailable(DetailedTotalMoneyMapper.map(globalStatsPerCurrency, ProjectStatPerCurrencyReadEntity::totalAvailable))
                .totalGranted(DetailedTotalMoneyMapper.map(globalStatsPerCurrency, ProjectStatPerCurrencyReadEntity::totalGranted))
                .totalRewarded(DetailedTotalMoneyMapper.map(globalStatsPerCurrency, ProjectStatPerCurrencyReadEntity::totalRewarded))
                ;
    }
}
