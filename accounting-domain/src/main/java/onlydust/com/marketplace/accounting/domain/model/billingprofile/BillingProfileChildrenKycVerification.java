package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.NonNull;

public record BillingProfileChildrenKycVerification(
        @NonNull
        BillingProfile.Id billingProfileId,
        @NonNull String billingProfileName,
        @NonNull
        IndividualKycIdentity individualKycIdentity,
        @NonNull
        String externalLinkForVerification
) {
}
