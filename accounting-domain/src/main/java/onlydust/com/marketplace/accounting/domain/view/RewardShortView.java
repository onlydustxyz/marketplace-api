package onlydust.com.marketplace.accounting.domain.view;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;

@SuperBuilder
@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
public class RewardShortView {
    private final @NonNull RewardId id;
    private final @NonNull RewardStatus status;
    private final @NonNull ShortProjectView project;
    private final @NonNull MoneyView money;
}
