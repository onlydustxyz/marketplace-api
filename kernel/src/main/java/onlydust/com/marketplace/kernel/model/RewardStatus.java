package onlydust.com.marketplace.kernel.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.UUID;

@Value
@Builder
public class RewardStatus {
    @NonNull UUID projectId;
    UUID billingProfileId;
    @NonNull Long recipientId;
    @NonNull RewardStatus.Input status;

    public Output as(AuthenticatedUser user) {
        if (user.getRoles().contains(AuthenticatedUser.Role.INTERNAL_SERVICE) || user.getAdministratedBillingProfiles().contains(billingProfileId)) {
            return asBillingProfileAdmin();
        }

        if (user.getGithubUserId().equals(recipientId)) {
            return asRecipient();
        }

        if (user.getProjectsLed().contains(projectId)) {
            return asProjectLead();
        }

        throw OnlyDustException.forbidden("User %s is not authorized to view this reward status".formatted(user.getId()));
    }

    public enum Input {
        PENDING_SIGNUP, PENDING_BILLING_PROFILE, PENDING_VERIFICATION, GEO_BLOCKED, INDIVIDUAL_LIMIT_REACHED,
        PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST, PROCESSING, COMPLETE
    }

    public enum Output {
        PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE, PENDING_COMPANY, PENDING_VERIFICATION, GEO_BLOCKED, INDIVIDUAL_LIMIT_REACHED,
        PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST, PROCESSING, COMPLETE
    }

    private Output asBillingProfileAdmin() {
        return Output.valueOf(status.toString());
    }

    private Output asRecipient() {
        return switch (status) {
            case PENDING_VERIFICATION, GEO_BLOCKED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST -> Output.PENDING_COMPANY;
            default -> Output.valueOf(status.toString());
        };
    }

    private Output asProjectLead() {
        return switch (status) {
            case PENDING_BILLING_PROFILE, PENDING_VERIFICATION, GEO_BLOCKED, INDIVIDUAL_LIMIT_REACHED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST ->
                    Output.PENDING_CONTRIBUTOR;
            default -> Output.valueOf(status.toString());
        };
    }

    public boolean isPendingRequest() {
        return status == Input.PENDING_REQUEST;
    }

    public String toString() {
        return status.toString();
    }
}
