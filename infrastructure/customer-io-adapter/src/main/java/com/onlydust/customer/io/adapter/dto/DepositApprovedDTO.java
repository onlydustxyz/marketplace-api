package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.DepositApproved;

public record DepositApprovedDTO(
        @NonNull String title,
        @NonNull String username,
        @NonNull String description,
        @NonNull ButtonDTO button
) {

    private static final String DESCRIPTION = "Your deposit has been successfully approved. The funds are now available in your account. " +
                                              "You can view the details in your dashboard.";

    public static DepositApprovedDTO from(final String recipientLogin, final DepositApproved depositApproved, final String environment) {
        return new DepositApprovedDTO("Deposit approved", recipientLogin, DESCRIPTION, new ButtonDTO(
                "Review transaction details",
                UrlMapper.getMarketplaceAdminFrontendUrlFromEnvironment(environment) + "financials/" + depositApproved.sponsorId())
        );
    }
}
