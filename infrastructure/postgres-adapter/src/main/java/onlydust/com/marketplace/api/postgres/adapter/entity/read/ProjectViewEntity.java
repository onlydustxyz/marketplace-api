package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoLanguageViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RepoMapper;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationView;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Entity
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Table(name = "projects", schema = "public")
@Immutable
public class ProjectViewEntity {
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
    List<GithubRepoViewEntity> repos;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    Set<ProjectMoreInfoViewEntity> moreInfos;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    Set<ProjectTagViewEntity> tags;


    public Project toDomain() {
        return Project.builder()
                .id(id)
                .slug(slug)
                .name(name)
                .shortDescription(shortDescription)
                .longDescription(longDescription)
                .logoUrl(logoUrl)
                .moreInfoUrl(moreInfos.stream().findFirst().map(ProjectMoreInfoViewEntity::getUrl).orElse(null))
                .hiring(hiring)
                .visibility(visibility)
                .build();
    }

    public ProjectView toBoView() {
        return ProjectView.builder()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .build();
    }

    public ProjectShortView toView() {
        return ProjectShortView.builder()
                .slug(slug)
                .name(name)
                .logoUrl(logoUrl)
                .shortDescription(shortDescription)
                .id(ProjectId.of(id))
                .build();
    }

    public Map<String, Long> technologies() {
        return getRepos().stream()
                .flatMap(repo -> repo.getLanguages().stream())
                .collect(Collectors.groupingBy(GithubRepoLanguageViewEntity::getLanguage, Collectors.summingLong(GithubRepoLanguageViewEntity::getLineCount)));
    }

    public List<ProjectOrganizationView> organizations() {
        final var organizationEntities = new HashMap<Long, GithubAccountViewEntity>();
        getRepos().forEach(repo -> organizationEntities.put(repo.getOwner().id(), repo.getOwner()));

        final var repoIdsIncludedInProject =
                getRepos().stream()
                        .filter(GithubRepoViewEntity::isPublic)
                        .map(GithubRepoViewEntity::getId).collect(Collectors.toSet());

        return organizationEntities.values().stream().map(entity -> ProjectOrganizationView.builder()
                .id(entity.id())
                .login(entity.login())
                .avatarUrl(entity.avatarUrl())
                .htmlUrl(entity.htmlUrl())
                .name(entity.name())
                .installationId(isNull(entity.installation()) ? null : entity.installation().getId())
                .isInstalled(nonNull(entity.installation()))
                .repos(entity.repos().stream()
                        .filter(GithubRepoViewEntity::isPublic)
                        .map(repo -> RepoMapper.mapToDomain(repo,
                                repoIdsIncludedInProject.contains(repo.getId()),
                                entity.installation() != null &&
                                entity.installation().getAuthorizedRepos().stream()
                                        .anyMatch(installedRepo -> installedRepo.getId().getRepoId().equals(repo.getId())))
                        )
                        .collect(Collectors.toSet()))
                .build()).toList();
    }
}
