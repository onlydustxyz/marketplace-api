package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "contribution_type", typeClass = PostgreSQLEnumType.class)
public class RewardItemViewEntity {

    @Id
    @Column(name = "contribution_id")
    String id;
    @Column(name = "start_date")
    Date createdAt;
    @Column(name = "end_date")
    Date completedAt;
    @Enumerated(EnumType.STRING)
    @Type(type = "contribution_type")
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

    public enum ContributionType {
        ISSUE, PULL_REQUEST, CODE_REVIEW
    }
}
