package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.CompleteYourBillingProfile;

public record CompleteYourBillingProfileDTO(@NonNull String title,
                                            @NonNull String description,
                                            @NonNull String username,
                                            @NonNull ButtonDTO button,
                                            @NonNull Boolean hasMoreInformation) {

    private static final String DESCRIPTION = "You have started the creation of a billing profile %s, but we need additional information to validate it.";

    public static CompleteYourBillingProfileDTO from(@NonNull CompleteYourBillingProfile completeYourBillingProfile, @NonNull String username,
                                                     @NonNull String environment) {
        return new CompleteYourBillingProfileDTO("Complete your billing profile",
                DESCRIPTION.formatted(completeYourBillingProfile.billingProfile().billingProfileName()),
                username, new ButtonDTO("Resume my billing profile",
                UrlMapper.getMarketplaceFrontendUrlFromEnvironment(environment) +
                "settings/billing/%s/general-information".formatted(completeYourBillingProfile.billingProfile().billingProfileId())),
                true
        );
    }
}
