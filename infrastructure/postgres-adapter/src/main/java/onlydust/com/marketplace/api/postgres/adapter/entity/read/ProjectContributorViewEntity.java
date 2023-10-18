package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class ProjectContributorViewEntity {
    @Id
    @Column(name = "id")
    Long githubUserId;
    @Column(name = "login")
    String login;
    @Column(name = "avatar_url")
    String avatarUrl;
    @Column(name = "contribution_count")
    Integer contributionCount;
    @Column(name = "is_registered")
    boolean isRegistered;
    @Column(name = "earned")
    BigDecimal earned;
    @Column(name = "reward_count")
    Integer rewards;
    @Column(name = "to_reward_count")
    Integer totalToReward;
    @Column(name = "prs_to_reward")
    Integer prsToReward;
    @Column(name = "code_reviews_to_reward")
    Integer codeReviewsToReward;
    @Column(name = "issues_to_reward")
    Integer issuesToReward;
}
