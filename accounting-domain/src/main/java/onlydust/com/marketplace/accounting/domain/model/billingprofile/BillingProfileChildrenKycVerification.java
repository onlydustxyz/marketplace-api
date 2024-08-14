package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.dto.NotificationBillingProfile;

public record BillingProfileChildrenKycVerification(
        @NonNull
        NotificationBillingProfile billingProfile,
        @NonNull
        IndividualKycIdentity individualKycIdentity,
        @NonNull
        String externalLinkForVerification
) {
}
