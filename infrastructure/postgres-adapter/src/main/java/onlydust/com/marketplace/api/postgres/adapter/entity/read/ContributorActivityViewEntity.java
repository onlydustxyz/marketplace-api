package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.view.ContributorActivityView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@EqualsAndHashCode
@Data
@Entity
@Immutable
public class ContributorActivityViewEntity {
    @Id
    Long id;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    Integer completedPullRequestCount;
    Integer completedIssueCount;
    Integer completedCodeReviewCount;
    @JdbcTypeCode(SqlTypes.JSON)
    List<UserProfileEntity.WeekCount> counts;

    public ContributorActivityView toDomain() {
        return ContributorActivityView.builder()
                .githubId(id)
                .login(login)
                .htmlUrl(htmlUrl)
                .avatarUrl(avatarUrl)
                .isRegistered(isRegistered)
                .completedPullRequestCount(completedPullRequestCount)
                .completedIssueCount(completedIssueCount)
                .completedCodeReviewCount(completedCodeReviewCount)
                .contributionStats(counts.stream().map(UserProfileEntity.WeekCount::toDomain).toList())
                .build();
    }
}
