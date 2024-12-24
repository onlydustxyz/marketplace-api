package onlydust.com.marketplace.api.read.entities.project;

import java.math.BigDecimal;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ContributorPageItemResponseV2;
import onlydust.com.marketplace.api.contract.model.RankedContributorResponse;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Immutable
public class ProjectContributorV2ReadEntity {
    @Id
    private Long id;
    @JdbcTypeCode(SqlTypes.JSON)
    private RankedContributorResponse contributor;
    private Integer mergedPullRequestCount;
    private Integer rewardCount;
    private BigDecimal totalEarnedUsdAmount;

    public ContributorPageItemResponseV2 toResponse() {
        return new ContributorPageItemResponseV2()
                .githubUserId(contributor.getGithubUserId()) 
                .login(contributor.getLogin())
                .avatarUrl(contributor.getAvatarUrl())
                .isRegistered(contributor.getIsRegistered())
                .id(contributor.getId())
                .globalRank(contributor.getGlobalRank())
                .globalRankPercentile(contributor.getGlobalRankPercentile())
                .globalRankCategory(contributor.getGlobalRankCategory())
                .mergedPullRequestCount(mergedPullRequestCount)
                .rewardCount(rewardCount)
                .totalEarnedUsdAmount(totalEarnedUsdAmount);
    }
}
