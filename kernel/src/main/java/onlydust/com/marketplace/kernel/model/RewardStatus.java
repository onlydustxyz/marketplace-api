package onlydust.com.marketplace.kernel.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@RequiredArgsConstructor
public enum RewardStatus {
    PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE, PENDING_COMPANY, PENDING_VERIFICATION, PAYMENT_BLOCKED, PAYOUT_INFO_MISSING, LOCKED,
    PENDING_REQUEST, PROCESSING, COMPLETE;

    public RewardStatus asProjectLead() {
        return switch (this) {
            case PENDING_SIGNUP -> PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE, PENDING_VERIFICATION, PAYMENT_BLOCKED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST -> PENDING_CONTRIBUTOR;
            case PROCESSING -> PROCESSING;
            case COMPLETE -> COMPLETE;
            case PENDING_CONTRIBUTOR, PENDING_COMPANY -> throw internalServerError("Impossible %s status as project lead".formatted(this.name()));
        };
    }

    public RewardStatus asBackofficeUser() {
        return switch (this) {
            case PENDING_SIGNUP -> PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> PENDING_VERIFICATION;
            case PAYMENT_BLOCKED -> PAYMENT_BLOCKED;
            case PAYOUT_INFO_MISSING -> PAYOUT_INFO_MISSING;
            case LOCKED -> LOCKED;
            case PENDING_REQUEST -> PENDING_REQUEST;
            case PROCESSING -> PROCESSING;
            case COMPLETE -> COMPLETE;
            case PENDING_CONTRIBUTOR, PENDING_COMPANY -> throw internalServerError("Impossible %s status as backoffice user".formatted(this.name()));
        };
    }

    RewardStatus asRecipient() {
        return switch (this) {
            case PENDING_BILLING_PROFILE -> PENDING_BILLING_PROFILE;
            case PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_COMPANY, PENDING_VERIFICATION, PAYMENT_BLOCKED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST,
                 PROCESSING, COMPLETE -> throw internalServerError("Impossible %s status as recipient".formatted(this.name()));
        };
    }

    RewardStatus asBillingProfileAdmin() {
        return switch (this) {
            case PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE, PENDING_COMPANY ->
                    throw internalServerError("Impossible %s status as billing profile admin".formatted(this.name()));
            case PENDING_VERIFICATION -> PENDING_VERIFICATION;
            case PAYMENT_BLOCKED -> PAYMENT_BLOCKED;
            case PAYOUT_INFO_MISSING -> PAYOUT_INFO_MISSING;
            case LOCKED -> LOCKED;
            case PENDING_REQUEST -> PENDING_REQUEST;
            case PROCESSING -> PROCESSING;
            case COMPLETE -> COMPLETE;
        };
    }

    RewardStatus asBillingProfileMember() {
        return switch (this) {
            case PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE, PENDING_COMPANY ->
                    throw internalServerError("Impossible %s status as billing profile member".formatted(this.name()));
            case PENDING_VERIFICATION -> PENDING_COMPANY;
            case PAYMENT_BLOCKED -> PENDING_COMPANY;
            case PAYOUT_INFO_MISSING -> PENDING_COMPANY;
            case LOCKED -> PENDING_COMPANY;
            case PENDING_REQUEST -> PENDING_COMPANY;
            case PROCESSING -> PROCESSING;
            case COMPLETE -> COMPLETE;
        };
    }

    public RewardStatus getRewardStatusForUser(final UUID rewardId, final Long rewardRecipientId, final UUID rewardBillingProfileId,
                                               final Long userGithubUserId, final List<UserBillingProfile> billingProfiles) {
        if (isNull(rewardBillingProfileId) && rewardRecipientId.equals(userGithubUserId)) {
            return this.asRecipient();
        }
        if (billingProfiles.stream()
                .filter(bp -> bp.role() == UserBillingProfile.Role.ADMIN)
                .map(UserBillingProfile::id)
                .toList().contains(rewardBillingProfileId)) {
            return this.asBillingProfileAdmin();
        }
        if (billingProfiles.stream()
                .filter(bp -> bp.role() == UserBillingProfile.Role.MEMBER)
                .map(UserBillingProfile::id)
                .toList().contains(rewardBillingProfileId)) {
            return this.asBillingProfileMember();
        }
        throw internalServerError("Cannot map reward %s to correct reward status %s because no condition was matched".formatted(rewardId,
                this));
    }

    @Builder
    public record UserBillingProfile(@NonNull UUID id, @NonNull Role role) {
        public enum Role {
            ADMIN, MEMBER;
        }
    }
}
