package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.FundsUngrantedFromProject;

public record FundsUngrantedFromProjectDTO(
        @NonNull String title,
        @NonNull String username,
        @NonNull String description,
        @NonNull ButtonDTO button
) {

    private static final String DESCRIPTION = "A grant has been returned to you from a project. The funds have been credited back to your account. " +
                                              "You can review the details of this transaction on your dashboard.";

    public static FundsUngrantedFromProjectDTO from(final String recipientLogin, final FundsUngrantedFromProject fundsUngrantedFromProject,
                                                    final String environment) {
        return new FundsUngrantedFromProjectDTO("Grant returned from project", recipientLogin, DESCRIPTION, new ButtonDTO(
                "Review transaction details",
                UrlMapper.getMarketplaceAdminFrontendUrlFromEnvironment(environment) + "programs/" + fundsUngrantedFromProject.programId())
        );
    }
}
