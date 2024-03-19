package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
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
    boolean isHidden;
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
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "totals_earned")
    private List<UserProfileEntity.TotalEarnedPerCurrency> totalEarnedPerCurrencies;
}
