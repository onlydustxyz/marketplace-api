package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationSuccessfullyCreated;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record NewCommitteeApplicationDTO(@NonNull UUID projectId,
                                         @NonNull String projectName,
                                         @NonNull UUID committeeId,
                                         @NonNull String committeeName,
                                         @NonNull String username,
                                         @NonNull String applicationEndDate) {

    public static NewCommitteeApplicationDTO fromEvent(@NonNull final String recipientLogin,
                                                       @NonNull final CommitteeApplicationSuccessfullyCreated event) {
        return new NewCommitteeApplicationDTO(
                event.getProjectId(), event.getProjectName(),
                event.getCommitteeId(), event.getCommitteeName(),
                recipientLogin,
                event.getApplicationEndDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss z").withZone(ZoneId.systemDefault())));
    }
}
