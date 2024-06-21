package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.contract.model.ShortGithubRepoResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoLanguageViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoStatsViewEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(schema = "indexer_exp", name = "github_repos")
@Immutable
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "repoId")
    List<GithubRepoLanguageViewEntity> languages;

    @ManyToOne(fetch = FetchType.LAZY)
    GithubRepoReadEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_github_repos",
            schema = "public",
            joinColumns = @JoinColumn(name = "github_repo_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    ProjectViewEntity project;
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
