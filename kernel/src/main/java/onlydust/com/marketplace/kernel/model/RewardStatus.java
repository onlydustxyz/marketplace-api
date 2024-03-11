package onlydust.com.marketplace.kernel.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RewardStatus {
    public static final RewardStatus PENDING_SIGNUP = new RewardStatus(AsUser.PENDING_SIGNUP);
    public static final RewardStatus PENDING_BILLING_PROFILE = new RewardStatus(AsUser.PENDING_BILLING_PROFILE);
    public static final RewardStatus PENDING_VERIFICATION = new RewardStatus(AsUser.PENDING_VERIFICATION);
    public static final RewardStatus PAYMENT_BLOCKED = new RewardStatus(AsUser.PAYMENT_BLOCKED);
    public static final RewardStatus PAYOUT_INFO_MISSING = new RewardStatus(AsUser.PAYOUT_INFO_MISSING);
    public static final RewardStatus LOCKED = new RewardStatus(AsUser.LOCKED);
    public static final RewardStatus PENDING_REQUEST = new RewardStatus(AsUser.PENDING_REQUEST);
    public static final RewardStatus PROCESSING = new RewardStatus(AsUser.PROCESSING);
    public static final RewardStatus COMPLETE = new RewardStatus(AsUser.COMPLETE);
    
    public enum AsUser {
        PENDING_SIGNUP, PENDING_BILLING_PROFILE, PENDING_VERIFICATION, PAYMENT_BLOCKED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST, PROCESSING, COMPLETE;
    }

    public enum AsProjectLead {
        PENDING_SIGNUP, PENDING_CONTRIBUTOR, PROCESSING, COMPLETE
    }

    private final @NonNull AsUser status;

    public AsUser asUser() {
        return status;
    }

    public AsProjectLead asProjectLead() {
        return switch (status) {
            case PENDING_SIGNUP -> AsProjectLead.PENDING_SIGNUP;
            case PROCESSING -> AsProjectLead.PROCESSING;
            case COMPLETE -> AsProjectLead.COMPLETE;
            default -> AsProjectLead.PENDING_CONTRIBUTOR;
        };
    }
}
