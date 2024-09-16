package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.FundsUnallocatedFromProgram;

public record FundsUnallocatedFromProgramDTO(
        @NonNull String title,
        @NonNull String username,
        @NonNull String description,
        @NonNull ButtonDTO button
) {

    private static final String DESCRIPTION = "An allocation has been returned to you from a program. The funds have been credited back to your account. " +
                                              "You can review the details of this transaction dashboard.";

    public static FundsUnallocatedFromProgramDTO from(final String recipientLogin, final FundsUnallocatedFromProgram fundsUnallocatedFromProgram,
                                                      final String environment) {
        return new FundsUnallocatedFromProgramDTO("Allocation returned from program", recipientLogin, DESCRIPTION, new ButtonDTO(
                "Review transaction details",
                UrlMapper.getMarketplaceAdminFrontendUrlFromEnvironment(environment) + "financials/" + fundsUnallocatedFromProgram.sponsorId())
        );
    }
}
