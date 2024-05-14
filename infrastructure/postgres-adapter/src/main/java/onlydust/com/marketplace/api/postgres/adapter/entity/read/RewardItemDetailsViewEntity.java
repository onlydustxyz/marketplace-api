package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class RewardItemDetailsViewEntity {

    @Id
    @Column(name = "reward_id")
    String id;
    @Column(name = "contribution_id")
    String contributionId;
    @Column(name = "start_date")
    Date createdAt;
    @Column(name = "end_date")
    Date completedAt;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "contribution_type")
    ContributionType type;
    @Column(name = "status")
    String status;
    @Column(name = "number")
    Long number;
    @Column(name = "title")
    String title;
    @Column(name = "html_url")
    String githubUrl;
    @Column(name = "repo_name")
    String repoName;
    @Column(name = "author_id")
    Long authorId;
    @Column(name = "author_login")
    String authorLogin;
    @Column(name = "author_avatar_url")
    String authorAvatarUrl;
    @Column(name = "author_github_url")
    String authorProfileUrl;
    @Column(name = "commits_count")
    Integer commitsCount;
    @Column(name = "user_commits_count")
    Integer userCommitsCount;
    @Column(name = "comments_count")
    Integer commentsCount;
    @Column(name = "recipient_id")
    Long recipientId;
    @Column(name = "github_body")
    String githubBody;
    UUID billingProfileId;

    public enum ContributionType {
        ISSUE, PULL_REQUEST, CODE_REVIEW
    }
}
