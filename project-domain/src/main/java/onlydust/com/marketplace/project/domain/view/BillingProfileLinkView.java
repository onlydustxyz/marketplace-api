package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.util.UUID;

@Builder
@Value
@Accessors(fluent = true)
public class BillingProfileLinkView {
    UUID id;
    Type type;
    Role role;
    VerificationStatus verificationStatus;
    Boolean missingPayoutInfo;
    Boolean missingVerification;

    public enum Type {
        INDIVIDUAL, COMPANY, SELF_EMPLOYED
    }

    public enum Role {
        ADMIN, MEMBER;
    }

    public enum VerificationStatus {
        VERIFIED, UNDER_REVIEW, STARTED, NOT_STARTED, REJECTED, CLOSED;

        public boolean isBlocked() {
            return this == VerificationStatus.REJECTED || this == VerificationStatus.CLOSED;
        }
    }

    public boolean isVerificationBlocked() {
        return verificationStatus.isBlocked();
    }

    public RewardStatus.UserBillingProfile toUserBillingProfile() {
        return RewardStatus.UserBillingProfile.builder()
                .id(this.id)
                .role(switch (this.role) {
                    case ADMIN -> RewardStatus.UserBillingProfile.Role.ADMIN;
                    case MEMBER -> RewardStatus.UserBillingProfile.Role.MEMBER;
                })
                .build();
    }
}
