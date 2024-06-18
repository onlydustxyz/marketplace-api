package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.contract.model.GithubOrganizationInstallationStatus;
import onlydust.com.marketplace.api.contract.model.GithubOrganizationResponse;
import onlydust.com.marketplace.api.contract.model.GithubRepoResponse;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectMoreInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectSponsorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectTagViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.bff.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "projects", schema = "public")
@Immutable
public class ProjectReadEntity {
    @Id
    @EqualsAndHashCode.Include
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    Set<ProjectSponsorViewEntity> sponsors = new HashSet<>();

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
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "userId")
    )
    Set<AllUserReadEntity> leads;

    @Formula("(select count(pgfi.issue_id) from projects_good_first_issues pgfi where pgfi.project_id = id)")
    Integer goodFirstIssueCount;

    public List<GithubOrganizationResponse> organizations() {
        final var organizationEntities = new HashMap<Long, GithubAccountViewEntity>();
        getRepos().forEach(repo -> organizationEntities.put(repo.getOwner().id(), repo.getOwner()));

        final var repoIdsIncludedInProject = getRepos().stream()
                .filter(GithubRepoViewEntity::isPublic)
                .map(GithubRepoViewEntity::getId)
                .collect(Collectors.toSet());

        return organizationEntities.values().stream().map(entity -> new GithubOrganizationResponse()
                .githubUserId(entity.id())
                .login(entity.login())
                .avatarUrl(entity.avatarUrl())
                .htmlUrl(nonNull(entity.htmlUrl()) ? URI.create(entity.htmlUrl()) : null)
                .name(entity.name())
                .installationId(entity.installation() != null ? entity.installation().getId() : null)
                .installationStatus(entity.installation() == null ? GithubOrganizationInstallationStatus.NOT_INSTALLED : switch (entity.installation().getStatus()) {
                    case SUSPENDED -> GithubOrganizationInstallationStatus.SUSPENDED;
                    case MISSING_PERMISSIONS -> GithubOrganizationInstallationStatus.MISSING_PERMISSIONS;
                    case COMPLETE -> GithubOrganizationInstallationStatus.COMPLETE;
                })
                .repos(entity.repos().stream()
                        .filter(GithubRepoViewEntity::isPublic)
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
        ).toList();
    }

    public ProjectLinkResponse toLinkResponse() {
        return new ProjectLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .slug(slug);
    }
}
