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

    public static PayableReward of(@NonNull RewardId id, @NonNull PayableCurrency currency, @NonNull PositiveAmount amount, @NonNull Invoice.BillingProfileSnapshot billingProfileSnapshot) {
        return new PayableReward(id, currency, amount,
                billingProfileSnapshot.wallet(currency.network()).orElseThrow(),
                billingProfileSnapshot.subject());
    }

    public PayableReward add(final PayableReward other) {
        return new PayableReward(id, currency, amount.add(other.amount), recipientWallet, recipientName);
    }
}

