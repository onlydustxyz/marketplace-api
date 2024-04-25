package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

@Builder(toBuilder = true)
public record ShortRewardDetailsView(
        @NonNull RewardId id,
        @NonNull ProjectShortView project,
        @NonNull MoneyView money,
        @NonNull ShortContributorView recipient,
        @NonNull ShortContributorView requester
) {
}
