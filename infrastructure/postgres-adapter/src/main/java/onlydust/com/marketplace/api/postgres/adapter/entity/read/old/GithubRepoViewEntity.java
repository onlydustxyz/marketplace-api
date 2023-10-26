package onlydust.com.marketplace.api.postgres.adapter.entity.read.old;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity(name = "OldGithubRepoViewEntity")
@Table(name = "github_repos", schema = "public")
public class GithubRepoViewEntity {
    @Id
    @Column(name = "id", nullable = false)
    Long githubId;
    @Column(name = "owner", nullable = false)
    String owner;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "updated_at")
    ZonedDateTime updatedAt;
    @Column(name = "description", nullable = false)
    String description;
    @Column(name = "stars", nullable = false)
    Integer starCount;
    @Column(name = "fork_count", nullable = false)
    Integer forkCount;
    @Column(name = "html_url", nullable = false)
    String htmlUrl;
    @Column(name = "languages", nullable = false)
    String languages;
    @Column(name = "parent_id")
    Long parentId;
    @Column(name = "has_issues", nullable = false)
    Boolean hasIssues;
}
