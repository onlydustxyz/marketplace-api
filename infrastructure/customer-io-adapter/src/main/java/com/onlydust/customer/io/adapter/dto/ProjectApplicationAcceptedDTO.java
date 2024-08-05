package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;

public record ProjectApplicationAcceptedDTO(@NonNull String username,
                                            @NonNull String projectName,
                                            @NonNull Long issueId,
                                            @NonNull String issueUrl,
                                            @NonNull String repoName,
                                            @NonNull String issueTitle,
                                            String issueDescription) {

    public static ProjectApplicationAcceptedDTO fromEvent(String recipientLogin, final ApplicationAccepted event) {
        return new ProjectApplicationAcceptedDTO(
                recipientLogin,
                event.getProject().name(),
                event.getIssue().id(),
                event.getIssue().htmlUrl(),
                event.getIssue().repoName(),
                event.getIssue().title(),
                event.getIssue().description());
    }
}
