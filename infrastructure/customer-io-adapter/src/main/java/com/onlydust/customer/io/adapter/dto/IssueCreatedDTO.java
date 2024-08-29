package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.notification.GoodFirstIssueCreated;

import java.util.List;

public record IssueCreatedDTO(
        @NonNull String title,
        @NonNull String username,
        @NonNull String description,
        @NonNull ButtonDTO button,
        @NonNull IssueDetailsDTO issue
) {

    private static final String DESCRIPTION = "We are excited to inform you that a new issue has been posted on the project <b>%s</b> you subscribed to." +
                                              " You can view the details of the issue by clicking the link below.";

    public static IssueCreatedDTO from(final String recipientLogin, final GoodFirstIssueCreated goodFirstIssueCreated, final String environment) {
        return new IssueCreatedDTO("New good first issue", recipientLogin, DESCRIPTION.formatted(goodFirstIssueCreated.getProject().name()), new ButtonDTO(
                "View issue",
                UrlMapper.getMarketplaceFrontendUrlFromEnvironment(environment) + "p/" + goodFirstIssueCreated.getProject().slug()),
                new IssueDetailsDTO(goodFirstIssueCreated.getIssue().title(), "Today", goodFirstIssueCreated.getIssue().repoName(),
                        goodFirstIssueCreated.getIssue().labels(),
                        new IssueAuthorDTO(goodFirstIssueCreated.getIssue().authorLogin(), goodFirstIssueCreated.getIssue().authorAvatarUrl()))
        );
    }


    public record IssueDetailsDTO(@NonNull String title, @NonNull String createdAt, @NonNull String repository, @NonNull List<String> tags,
                                  @NonNull IssueAuthorDTO createdBy) {
    }

    public record IssueAuthorDTO(@NonNull String name, @NonNull String avatarUrl) {
    }
}
