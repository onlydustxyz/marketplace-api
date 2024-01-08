package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(schema = "indexer_exp", name = "github_repos")
@TypeDef(name = "github_repo_visibility", typeClass = PostgreSQLEnumType.class)
@Immutable
public class GithubRepoEntity {
    @Id
    @EqualsAndHashCode.Include
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    GithubAccountEntity owner;
    String name;
    String htmlUrl;
    Date updatedAt;
    String Description;
    Long starsCount;
    Long forksCount;
    Boolean hasIssues;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "repoId")
    List<GithubRepoLanguageEntity> languages;

    @ManyToOne(fetch = FetchType.LAZY)
    GithubRepoEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_github_repos",
            schema = "public",
            joinColumns = @JoinColumn(name = "github_repo_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    ProjectEntity project;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "github_repo_visibility")
    Visibility visibility;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    GithubRepoStatsEntity stats;

    public enum Visibility {
        PUBLIC, PRIVATE
    }

    public Boolean isPublic() {
        return nonNull(this.visibility) && this.visibility.equals(Visibility.PUBLIC);
    }
}
