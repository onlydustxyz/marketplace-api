package onlydust.com.marketplace.accounting.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Setter
@Getter
@Accessors(fluent = true, chain = true)
public class RewardStatus {
    private static final PositiveAmount INDIVIDUAL_THRESHOLD_USD = PositiveAmount.of(5000L);

    @NonNull
    final RewardId rewardId;

    Boolean isIndividual;
    @NonNull Boolean kycbVerified = false;
    Boolean usRecipient;
    @NonNull Currency rewardCurrency;
    @NonNull PositiveAmount rewardAmountUsdEquivalent;
    @NonNull PositiveAmount currentYearUsdTotal;
    @NonNull Boolean payoutInfoFilled = false;
    @NonNull Boolean sponsorHasEnoughFund = false;
    ZonedDateTime unlockDate;
    @NonNull Boolean paymentRequested = false;
    @NonNull Boolean invoiceApproved = false;
    @NonNull Boolean paid = false;

    public RewardStatus(RewardId rewardId) {
        this.rewardId = rewardId;
    }
    
    public enum Status {
        PENDING_BILLING_PROFILE,
        PENDING_VERIFICATION,
        PAYMENT_BLOCKED,
        PAYOUT_INFO_MISSING,
        LOCKED,
        PENDING_REQUEST,
        PROCESSING,
        COMPLETE;
    }

    public Status get() {
        if (complete()) return Status.COMPLETE;
        if (missingBillingProfile()) return Status.PENDING_BILLING_PROFILE;
        if (missingIdentityVerification()) return Status.PENDING_VERIFICATION;
        if (paymentBlocked()) return Status.PAYMENT_BLOCKED;
        if (missingPayoutInfo()) return Status.PAYOUT_INFO_MISSING;
        if (rewardLocked()) return Status.LOCKED;
        if (pendingRequest()) return Status.PENDING_REQUEST;
        return Status.PROCESSING;
    }

    private boolean rewardLocked() {
        return !sponsorHasEnoughFund || (unlockDate != null && unlockDate.isAfter(ZonedDateTime.now()));
    }

    private boolean missingPayoutInfo() {
        return !Boolean.TRUE.equals(payoutInfoFilled);
    }

    private boolean missingIdentityVerification() {
        return !Boolean.TRUE.equals(kycbVerified);
    }

    public boolean missingBillingProfile() {
        return isIndividual == null;
    }

    private boolean paymentBlocked() {
        return strkForUsCitizen() || individualThresholdReached();
    }

    private boolean strkForUsCitizen() {
        return usRecipient && rewardCurrency.code().toString().equals("STRK");
    }

    private boolean individualThresholdReached() {
        return isIndividual && currentYearUsdTotal.add(rewardAmountUsdEquivalent).isStrictlyGreaterThan(INDIVIDUAL_THRESHOLD_USD);
    }

    private boolean pendingRequest() {
        return !invoiceApproved || !paymentRequested;
    }

    private boolean complete() {
        return paid;
    }
}

