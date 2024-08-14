package onlydust.com.marketplace.accounting.domain.notification.dto;

import lombok.NonNull;

import java.util.UUID;

public record NotificationBillingProfile(@NonNull UUID billingProfileId, @NonNull String billingProfileName) {
}
