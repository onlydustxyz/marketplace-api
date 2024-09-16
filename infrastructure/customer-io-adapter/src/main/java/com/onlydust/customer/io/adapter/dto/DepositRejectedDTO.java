package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.DepositRejected;

public record DepositRejectedDTO(
        @NonNull String title,
        @NonNull String username,
        @NonNull String description,
        @NonNull ButtonDTO button
) {

    private static final String DESCRIPTION = "We regret to inform you that your deposit has been rejected. " +
                                              "Please review the information provided and try again. If you need assistance, feel free to contact us.";

    public static DepositRejectedDTO from(final String recipientLogin, final DepositRejected depositRejected, final String environment) {
        return new DepositRejectedDTO("Deposit refused", recipientLogin, DESCRIPTION, new ButtonDTO(
                "Review transaction details",
                UrlMapper.getMarketplaceAdminFrontendUrlFromEnvironment(environment) + "financials/" + depositRejected.sponsorId())
        );
    }
}
