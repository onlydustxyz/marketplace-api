package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class UserProfileEntity {
    @Id
    @Column(name = "row_number", nullable = false)
    Integer rowNumber;
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "github_user_id", nullable = false)
    Integer githubId;
    @Column(name = "email")
    String email;
    @Column(name = "bio")
    String bio;
    @Column(name = "avatar_url", nullable = false)
    String avatarUrl;
    @Column(name = "login", nullable = false)
    String login;
    @Column(name = "html_url")
    String htmlUrl;
    @Column(name = "location")
    String location;
    @Column(name = "website")
    String website;
    @Column(name = "public")
    Boolean contactPublic;
    @Column(name = "channel")
    String contactChannel;
    @Column(name = "contact")
    String contact;
    @Column(name = "languages")
    String languages;
    @Column(name = "cover")
    String cover;
    @Column(name = "last_seen")
    Date lastSeen;
    @Column(name = "created_at")
    Date createdAt;
    @Column(name = "code_review_count")
    Integer codeReviewCount;
    @Column(name = "issue_count")
    Integer issueCount;
    @Column(name = "pull_request_count")
    Integer pullRequestCount;
    @Column(name = "week")
    Integer week;
    @Column(name = "year")
    Integer year;
    @Column(name = "leading_project_number")
    Integer numberOfLeadingProject;
    @Column(name = "contributor_on_project")
    Integer numberOfOwnContributorOnProject;
    @Column(name = "contributions_count")
    Integer contributionsCount;
    @Column(name = "total_earned")
    BigDecimal totalEarned;

}
