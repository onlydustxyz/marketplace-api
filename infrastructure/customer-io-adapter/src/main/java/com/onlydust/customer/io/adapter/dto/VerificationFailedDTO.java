package com.onlydust.customer.io.adapter.dto;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.BillingProfileVerificationFailed;

@Builder
public record VerificationFailedDTO(@NonNull String status, @NonNull String username, @NonNull String billingProfileId) {

    public static VerificationFailedDTO fromEvent(final String recipientLogin, final BillingProfileVerificationFailed billingProfileVerificationFailed) {
        return VerificationFailedDTO.builder()
                .status(billingProfileVerificationFailed.verificationStatus().name())
                .billingProfileId(billingProfileVerificationFailed.billingProfileId().toString())
                .username(recipientLogin)
                .build();
    }
}
