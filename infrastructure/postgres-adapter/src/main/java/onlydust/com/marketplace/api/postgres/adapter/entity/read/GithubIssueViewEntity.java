package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountEntity;
import onlydust.com.marketplace.project.domain.view.GithubIssueView;
import org.hibernate.annotations.Type;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Value
@NoArgsConstructor(force = true)
@Table(schema = "indexer_exp", name = "github_issues")
public class GithubIssueViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull
    GithubRepoViewEntity repo;

    @NonNull
    Long number;
    @NonNull
    String title;

    @Enumerated(EnumType.STRING)
    @Type(PostgreSQLEnumType.class)
    @Column(columnDefinition = "github_issue_status")
    @NonNull
    Status status;

    @NonNull
    ZonedDateTime createdAt;
    ZonedDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull
    GithubAccountEntity author;

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
    List<GithubLabelViewEntity> labels;

    public enum Status {
        OPEN, CANCELLED, COMPLETED;

        GithubIssueView.Status toView() {
            return switch (this) {
                case OPEN -> GithubIssueView.Status.OPEN;
                case COMPLETED -> GithubIssueView.Status.COMPLETED;
                case CANCELLED -> GithubIssueView.Status.CANCELLED;
            };
        }
    }

    public GithubIssueView toView() {
        return new GithubIssueView(
                id,
                number,
                title,
                status.toView(),
                createdAt,
                closedAt,
                htmlUrl,
                body,
                author.toContributorLinkView(),
                repo.toShortView(),
                commentsCount,
                labels().stream().map(GithubLabelViewEntity::toView).toList()
        );
    }

    @NonNull
    private List<GithubLabelViewEntity> labels() {
        return Optional.ofNullable(labels).orElse(List.of());
    }
}
