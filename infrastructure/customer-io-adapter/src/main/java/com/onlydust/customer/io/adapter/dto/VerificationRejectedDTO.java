package com.onlydust.customer.io.adapter.dto;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.BillingProfileVerificationRejected;

@Builder
public record VerificationRejectedDTO(@NonNull String title,
                                      @NonNull String description,
                                      @NonNull String username,
                                      @NonNull String billingProfileId,
                                      @NonNull ButtonDTO button,
                                      @NonNull Boolean hasMoreInformation,
                                      @NonNull String reason) {

    private static final String DESCRIPTION = "We regret to inform you that your billing named <b>\"%s\"</b> has been rejected." +
                                              " We require additional actions from you t for verification in order to complete the verification:";

    public static VerificationRejectedDTO fromEvent(final String recipientLogin, final BillingProfileVerificationRejected billingProfileVerificationRejected,
                                                    final String environment) {
        return VerificationRejectedDTO.builder()
                .billingProfileId(billingProfileVerificationRejected.billingProfileId().toString())
                .username(recipientLogin)
                .title("Billing profile %s verification rejected".formatted(billingProfileVerificationRejected.billingProfileName()))
                .description(DESCRIPTION.formatted(billingProfileVerificationRejected.billingProfileName()))
                .button(new ButtonDTO("Resume verification", UrlMapper.getMarketplaceFrontendUrlFromEnvironment(environment) +
                                                             "settings/billing/%s/general-information".formatted(billingProfileVerificationRejected.billingProfileId())))
                .hasMoreInformation(true)
                .reason(billingProfileVerificationRejected.rejectionReason())
                .build();
    }
}
