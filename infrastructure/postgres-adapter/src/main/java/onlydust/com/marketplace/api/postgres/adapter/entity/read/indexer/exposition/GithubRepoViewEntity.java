package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(schema = "indexer_exp", name = "github_repos")
@Immutable
public class GithubRepoViewEntity {
    @Id
    @EqualsAndHashCode.Include
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    GithubAccountViewEntity owner;
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
    GithubRepoViewEntity parent;

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

    public enum Visibility {
        PUBLIC, PRIVATE
    }

    public Boolean isPublic() {
        return nonNull(this.visibility) && this.visibility.equals(Visibility.PUBLIC);
    }
}
