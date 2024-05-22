package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.event.NewCommitteeApplication;

import java.util.UUID;

public record NewCommitteeApplicationDTO(@NonNull UUID projectId, @NonNull String projectName, @NonNull UUID committeeId, @NonNull String committeeName,
                                         @NonNull String username) {

    public static NewCommitteeApplicationDTO fromEvent(final NewCommitteeApplication newCommitteeApplication) {
        return new NewCommitteeApplicationDTO(newCommitteeApplication.getProjectId(), newCommitteeApplication.getProjectName(),
                newCommitteeApplication.getCommitteeId(), newCommitteeApplication.getCommitteeName(), newCommitteeApplication.getGithubLogin());
    }
}
