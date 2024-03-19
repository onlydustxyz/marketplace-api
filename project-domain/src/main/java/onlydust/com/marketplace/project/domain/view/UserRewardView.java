package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserRewardView {
    UUID id;
    UUID projectId;
    Integer numberOfRewardedContributions;
    Date requestedAt;
    Date processedAt;
    String rewardedOnProjectName;
    String rewardedOnProjectLogoUrl;
    RewardStatus status;
    Date unlockDate;
    Amount amount;

    @Data
    @Builder
    public static class Amount {
        BigDecimal total;
        CurrencyView currency;
        BigDecimal dollarsEquivalent;
    }

    @Data
    @Builder
    public static class Filters {
        List<UUID> currencies;
        List<UUID> projectIds;
        Date from;
        Date to;
    }
}
