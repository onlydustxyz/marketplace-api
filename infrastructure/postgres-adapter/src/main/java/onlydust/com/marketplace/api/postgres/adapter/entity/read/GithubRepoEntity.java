package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;

import javax.persistence.*;
import java.util.Date;

@EqualsAndHashCode
@Data
@Entity
@Table(schema = "indexer_exp", name = "github_repos")
public class GithubRepoEntity {
    @Id
    Long id;
    @ManyToOne
    GithubAccountEntity owner;
    String name;
    String htmlUrl;
    Date updatedAt;
    String Description;
    Long starsCount;
    Long forksCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_github_repos",
            schema = "public",
            joinColumns = @JoinColumn(name = "github_repo_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    ProjectEntity project;
}
