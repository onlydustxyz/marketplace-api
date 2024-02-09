package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;

public record PayableReward(
        @NonNull
        RewardId id,
        @NonNull
        PayableCurrency currency,
        @NonNull
        PositiveAmount amount
) {
    public ImmutablePair<RewardId, PayableCurrency> key() {
        return ImmutablePair.of(id, currency);
    }

    public PayableReward add(final PayableReward other) {
        return new PayableReward(id, currency, amount.add(other.amount));
    }
}

