package com.onlydust.customer.io.adapter.dto;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.BillingProfileVerificationClosed;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceBillingProfileUrlFromEnvironment;

@Builder
public record VerificationClosedDTO(@NonNull String title,
                                    @NonNull String description,
                                    @NonNull String username,
                                    @NonNull ButtonDTO button) {

    private static final String DESCRIPTION = "We regret to inform you that we cannot proceed with your verification request " +
                                              "on your billing profile %s, as it has failed." +
                                              " If you require further information or assistance, please do not hesitate to contact us.";

    public static VerificationClosedDTO fromEvent(final String recipientLogin, final BillingProfileVerificationClosed billingProfileVerificationClosed,
                                                  final String environment) {
        return VerificationClosedDTO.builder()
                .username(recipientLogin)
                .title("Billing profile %s verification closed".formatted(billingProfileVerificationClosed.billingProfileName()))
                .description(DESCRIPTION.formatted(billingProfileVerificationClosed.billingProfileName()))
                .button(new ButtonDTO("Contact us", getMarketplaceBillingProfileUrlFromEnvironment(environment,
                        billingProfileVerificationClosed.billingProfileId())))
                .build();
    }
}
