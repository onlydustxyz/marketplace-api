package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceMyApplicationsFromEnvironment;

public record ProjectApplicationAcceptedDTO(@NonNull String username,
                                            @NonNull String title,
                                            @NonNull String description,
                                            @NonNull IssueDTO issue) {

    private static final String DESCRIPTION = "We are excited to inform you that your application to the issue" +
                                              " <b>%s</b> in the <b>%s</b>" +
                                              " project has been assigned to you! Thank you for your interest and willingness to contribute to our project.";

    public static ProjectApplicationAcceptedDTO fromEvent(final String recipientLogin, final ApplicationAccepted event, final String environment) {
        return new ProjectApplicationAcceptedDTO(
                recipientLogin,
                "Issue application accepted",
                DESCRIPTION.formatted(event.getIssue().title(), event.getProject().name()),
                new IssueDTO(
                        event.getIssue().title(),
                        event.getIssue().description(),
                        event.getIssue().repoName(),
                        getMarketplaceMyApplicationsFromEnvironment(environment)
                ));
    }
}
