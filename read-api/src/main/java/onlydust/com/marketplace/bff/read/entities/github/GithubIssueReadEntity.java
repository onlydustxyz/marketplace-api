package onlydust.com.marketplace.bff.read.entities.github;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.api.contract.model.GithubIssue;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.bff.read.entities.project.ProjectReadEntity;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Value
@NoArgsConstructor(force = true)
@Table(schema = "indexer_exp", name = "github_issues")
@Immutable
public class GithubIssueReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull GithubRepoReadEntity repo;

    @NonNull
    Long number;
    @NonNull
    String title;

    @Enumerated(EnumType.STRING)
    @NonNull
    GithubIssueStatus status;

    @NonNull
    ZonedDateTime createdAt;
    ZonedDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull GithubAccountReadEntity author;

    @NonNull
    String htmlUrl;
    String body;
    @NonNull
    Integer commentsCount;
    @NonNull
    String repoOwnerLogin;
    @NonNull
    String repoName;
    @NonNull
    String repoHtmlUrl;
    @NonNull
    String authorLogin;
    @NonNull
    String authorHtmlUrl;
    @NonNull
    String authorAvatarUrl;

    @ManyToMany
    @JoinTable(
            schema = "indexer_exp",
            name = "github_issues_labels",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @OrderBy("name")
    List<GithubLabelReadEntity> labels;


    @ManyToMany
    @JoinTable(
            schema = "public",
            name = "projects_good_first_issues",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    Set<ProjectReadEntity> goodFirstIssueOf;

    public GithubIssue toDto() {
        return new GithubIssue()
                .id(id)
                .number(number)
                .repository(repo.toShortResponse())
                .createdAt(createdAt)
                .closedAt(closedAt)
                .title(title)
                .body(body)
                .htmlUrl(htmlUrl)
                .status(status)
                .author(author.toContributorResponse())
                .commentCount(commentsCount)
                .labels(labels.stream().map(GithubLabelReadEntity::toDto).toList())
                ;
    }
}
