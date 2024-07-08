package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationAccepted;

public record ProjectApplicationAcceptedDTO(@NonNull String username,
                                            @NonNull String projectName,
                                            @NonNull Long issueId,
                                            @NonNull String issueUrl,
                                            @NonNull String repoName,
                                            @NonNull String issueTitle,
                                            String issueDescription) {

    public static ProjectApplicationAcceptedDTO fromEvent(final ProjectApplicationAccepted event) {
        return new ProjectApplicationAcceptedDTO(
                event.getUserLogin(),
                event.getProject().name(),
                event.getIssue().id(),
                event.getIssue().htmlUrl(),
                event.getIssue().repoName(),
                event.getIssue().title(),
                event.getIssue().description());
    }
}
