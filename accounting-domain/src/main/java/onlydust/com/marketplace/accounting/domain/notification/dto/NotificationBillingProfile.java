package onlydust.com.marketplace.accounting.domain.notification.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;

import java.util.UUID;

public record NotificationBillingProfile(@NonNull UUID billingProfileId, @NonNull String billingProfileName, @NonNull VerificationStatus verificationStatus) {
}
