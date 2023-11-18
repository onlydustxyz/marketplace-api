package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@TypeDef(name = "contribution_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "github_code_review_outcome", typeClass = PostgreSQLEnumType.class)
public class RewardableItemViewEntity {

    @Id
    @Column(name = "id")
    String id;
    @Column(name = "contribution_id")
    String contributionId;
    @Column(name = "start_date")
    Date createdAt;
    @Column(name = "end_date")
    Date completedAt;
    @Enumerated(EnumType.STRING)
    @Type(type = "contribution_type")
    RewardableItemViewEntity.ContributionType type;
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
    @Column(name = "commits_count")
    Integer commitsCount;
    @Column(name = "user_commits_count")
    Integer userCommitsCount;
    @Column(name = "comments_count")
    Integer commentsCount;
    @Enumerated(EnumType.STRING)
    @Type(type = "github_code_review_outcome")
    @Column(name = "cr_outcome")
    CodeReviewOutcome outcome;
    @Column(name = "draft")
    Boolean draft;
    @Column(name = "ignored")
    Boolean ignored;

    public enum ContributionType {
        ISSUE, PULL_REQUEST, CODE_REVIEW
    }

    public enum CodeReviewOutcome {
        CHANGE_REQUESTED, APPROVED, DISMISSED, PENDING, COMMENTED
    }

}
