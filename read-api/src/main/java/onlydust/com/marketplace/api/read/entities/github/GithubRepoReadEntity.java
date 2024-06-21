package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ShortGithubRepoResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoStatsViewEntity;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLOrder;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Date;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@NoArgsConstructor(force = true)
@Entity
@Table(schema = "indexer_exp", name = "github_repos")
@Immutable
@Accessors(fluent = true)
public class GithubRepoReadEntity {
    @Id
    @EqualsAndHashCode.Include
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    GithubAccountReadEntity owner;
    String name;
    String htmlUrl;
    Date updatedAt;
    String description;
    Long starsCount;
    Long forksCount;
    Boolean hasIssues;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "repo_languages",
            schema = "public",
            joinColumns = @JoinColumn(name = "repoId"),
            inverseJoinColumns = @JoinColumn(name = "languageId"))
    @NonNull
    @SQLOrder("rank desc")
    List<LanguageReadEntity> languages;

    @ManyToOne(fetch = FetchType.LAZY)
    GithubRepoReadEntity parent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_github_repos",
            schema = "public",
            joinColumns = @JoinColumn(name = "github_repo_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    Set<ProjectViewEntity> projects;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "github_repo_visibility")
    Visibility visibility;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    GithubRepoStatsViewEntity stats;

    public ShortGithubRepoResponse toShortResponse() {
        return new ShortGithubRepoResponse()
                .id(id)
                .name(name)
                .description(description)
                .owner(owner.login())
                .htmlUrl(htmlUrl);
    }

    public enum Visibility {
        PUBLIC, PRIVATE
    }
}
