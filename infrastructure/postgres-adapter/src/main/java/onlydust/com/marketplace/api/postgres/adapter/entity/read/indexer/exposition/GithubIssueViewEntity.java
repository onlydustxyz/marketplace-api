package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Entity
@Table(schema = "indexer_exp", name = "github_issues")
@Immutable
public class GithubIssueViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

    @ManyToOne
    @NonNull
    GithubRepoViewEntity repo;
    @NonNull
    Long number;
    @NonNull
    String title;
    @Enumerated(EnumType.STRING)
    @NonNull
    Status status;
    @NonNull
    Date createdAt;
    Date closedAt;
    @ManyToOne
    GithubAccountViewEntity author;
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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "github_issues_assignees",
            schema = "indexer_exp",
            joinColumns = @JoinColumn(name = "issueId"),
            inverseJoinColumns = @JoinColumn(name = "userId"))
    @NonNull
    List<GithubAccountViewEntity> assignees;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "github_issues_labels",
            schema = "indexer_exp",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id"))
    @NonNull
    List<GithubLabelViewEntity> labels;

    public enum Status {
        OPEN, COMPLETED, CANCELLED
    }

    public GithubIssue toDomain() {
        return new GithubIssue(GithubIssue.Id.of(id), repo.id, number, assignees.size());
    }
}
