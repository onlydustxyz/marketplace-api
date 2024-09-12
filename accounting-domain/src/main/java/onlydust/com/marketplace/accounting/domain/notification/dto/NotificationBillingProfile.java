package onlydust.com.marketplace.accounting.domain.notification.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;

public record NotificationBillingProfile(@NonNull BillingProfile.Id billingProfileId,
                                         @NonNull String billingProfileName,
                                         @NonNull VerificationStatus verificationStatus) {
}
