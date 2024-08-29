package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;

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
    @NonNull
    Long number;
    @NonNull
    String title;
    @NonNull
    Date createdAt;
    Date closedAt;
    @NonNull
    String htmlUrl;
    String body;
    @NonNull
    String repoName;
    @NonNull
    @Column(name = "repo_id")
    Long repoId;

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
    List<GithubLabelViewEntity> labels;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    @NonNull
    GithubAccountViewEntity author;

    public enum Status {
        OPEN, COMPLETED, CANCELLED
    }

    public GithubIssue toDomain() {
        return new GithubIssue(GithubIssue.Id.of(id),
                repoId,
                number,
                title,
                body,
                htmlUrl,
                repoName,
                assignees.size(),
                author.login(),
                author.avatarUrl(),
                isNull(labels) ? List.of() : labels.stream().map(GithubLabelViewEntity::getName).sorted(String::compareTo).toList()
        );
    }
}
