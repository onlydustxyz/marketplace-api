package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.FundsAllocatedToProgram;

public record FundsAllocatedToProgramDTO(
        @NonNull String title,
        @NonNull String username,
        @NonNull String description,
        @NonNull ButtonDTO button
) {

    private static final String DESCRIPTION = "We are pleased to inform you that a new allocation has been granted to you. " +
                                              "You can now view the details of this allocation in your personal account.";

    public static FundsAllocatedToProgramDTO from(final String recipientLogin, final FundsAllocatedToProgram fundsAllocatedToProgram,
                                                  final String environment) {
        return new FundsAllocatedToProgramDTO("New allocation received", recipientLogin, DESCRIPTION, new ButtonDTO(
                "Review allocation",
                UrlMapper.getMarketplaceAdminFrontendUrlFromEnvironment(environment) + "programs/" + fundsAllocatedToProgram.programId())
        );
    }
}
