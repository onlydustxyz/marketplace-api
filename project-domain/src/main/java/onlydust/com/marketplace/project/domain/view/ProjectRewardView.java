package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjectRewardView {
    UUID id;
    Integer numberOfRewardedContributions;
    Date requestedAt;
    Date processedAt;
    ContributorLinkView rewardedUser;
    RewardStatus status;
    Date unlockDate;
    Money amount;

    @Builder
    @Data
    public static class Filters {
        @Builder.Default
        List<UUID> currencies = List.of();
        @Builder.Default
        List<Long> contributors = List.of();
        Date from;
        Date to;
    }
}
