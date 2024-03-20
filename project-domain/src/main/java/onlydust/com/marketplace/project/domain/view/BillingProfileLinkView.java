package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

@Builder
public record BillingProfileLinkView(@NonNull UUID id, @NonNull Type type, @NonNull Boolean hasValidPayoutMethods, @NonNull Boolean hasValidVerificationStatus,
                                     @NonNull VerificationStatus verificationStatus, @NonNull Role role) {


    public enum Type {
        INDIVIDUAL, COMPANY, SELF_EMPLOYED
    }

    public enum Role {
        ADMIN, MEMBER;
    }

    public enum VerificationStatus {
        VERIFIED, UNDER_REVIEW, STARTED, NOT_STARTED, REJECTED, CLOSED;
    }
}
