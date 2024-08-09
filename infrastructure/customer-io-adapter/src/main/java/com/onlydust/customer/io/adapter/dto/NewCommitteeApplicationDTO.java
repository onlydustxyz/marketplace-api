package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceFrontendUrlFromEnvironment;

public record NewCommitteeApplicationDTO(@NonNull String title,
                                         @NonNull String username,
                                         @NonNull String description,
                                         @NonNull ButtonDTO button) {

    private static final String DESCRIPTION = "Thank you for taking the time to send in your application for %s.<br />" +
                                              "We confirm that we have received your answers for the %s committee.<br />" +
                                              "Should you like to edit them, you have till %s to do so.";

    public static NewCommitteeApplicationDTO fromEvent(@NonNull final String recipientLogin,
                                                       @NonNull final CommitteeApplicationCreated event,
                                                       @NonNull final String environment) {
        final String applicationEndDate =
                event.getApplicationEndDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss z").withZone(ZoneId.systemDefault()));
        return new NewCommitteeApplicationDTO("Committee application", recipientLogin, DESCRIPTION.formatted(event.getProjectName(), event.getCommitteeName(),
                applicationEndDate),
                new ButtonDTO("Review my answer",
                        getMarketplaceFrontendUrlFromEnvironment(environment) + "c/%s/applicant?p=%s".formatted(event.getCommitteeId(), event.getProjectId())));
    }
}
