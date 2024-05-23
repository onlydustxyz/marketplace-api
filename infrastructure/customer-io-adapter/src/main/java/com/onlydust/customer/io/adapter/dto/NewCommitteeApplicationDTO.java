package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.event.NewCommitteeApplication;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record NewCommitteeApplicationDTO(@NonNull UUID projectId, @NonNull String projectName, @NonNull UUID committeeId, @NonNull String committeeName,
                                         @NonNull String username, @NonNull String applicationEndDate) {

    public static NewCommitteeApplicationDTO fromEvent(final NewCommitteeApplication newCommitteeApplication) {
        return new NewCommitteeApplicationDTO(newCommitteeApplication.getProjectId(), newCommitteeApplication.getProjectName(),
                newCommitteeApplication.getCommitteeId(), newCommitteeApplication.getCommitteeName(), newCommitteeApplication.getGithubLogin(),
                newCommitteeApplication.getApplicationEndDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss z").withZone(ZoneId.systemDefault())));
    }
}
