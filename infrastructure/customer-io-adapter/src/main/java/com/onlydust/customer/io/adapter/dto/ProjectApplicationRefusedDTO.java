package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.dto.ApplicationRefused;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceMyApplicationsFromEnvironment;

public record ProjectApplicationRefusedDTO(@NonNull String username,
                                           @NonNull String title,
                                           @NonNull String description,
                                           @NonNull ButtonDTO button) {

    private static final String DESCRIPTION = "Thank you for your interest in project <b>%s</b>.<br /><br />" +
                                              "We wanted to inform you that the issue %s you applied for <b>has been assigned to another candidate</b>." +
                                              " However, we encourage you to continue exploring other projects and applying to different opportunities" +
                                              " that match your skills and interests.";

    public static ProjectApplicationRefusedDTO fromEvent(final String recipientLogin, final ApplicationRefused event, final String environment) {
        return new ProjectApplicationRefusedDTO(
                recipientLogin,
                "Issue application refused",
                DESCRIPTION.formatted(event.getProject().name(),event.getIssue().title()),
                new ButtonDTO("Explore more projects", UrlMapper.getMarketplaceFrontendUrlFromEnvironment(environment) + "projects"));
    }
}
