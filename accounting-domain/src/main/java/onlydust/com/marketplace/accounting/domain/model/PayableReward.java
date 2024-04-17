package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import org.apache.commons.lang3.tuple.ImmutablePair;

public record PayableReward(
        @NonNull RewardId id,
        @NonNull PayableCurrency currency,
        @NonNull PositiveAmount amount,
        @NonNull Wallet recipientWallet,
        @NonNull String recipientName
) {
    public ImmutablePair<RewardId, PayableCurrency> key() {
        return ImmutablePair.of(id, currency);
    }

    public static PayableReward of(@NonNull RewardId id, @NonNull PayableCurrency currency, @NonNull PositiveAmount amount, @NonNull Invoice invoice) {
        return new PayableReward(id, currency, amount,
                invoice.billingProfileSnapshot().wallet(currency.network()).orElseThrow(),
                invoice.billingProfileSnapshot().subject());
    }

    public PayableReward add(final PayableReward other) {
        return new PayableReward(id, currency, amount.add(other.amount), recipientWallet, recipientName);
    }
}

