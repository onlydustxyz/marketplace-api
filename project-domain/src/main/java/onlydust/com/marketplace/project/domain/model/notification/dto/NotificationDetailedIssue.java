package onlydust.com.marketplace.project.domain.model.notification.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

import java.util.List;

public record NotificationDetailedIssue(@NonNull Long id,
                                        @NonNull String htmlUrl,
                                        @NonNull String title,
                                        @NonNull String repoName,
                                        String description,
                                        @NonNull String authorLogin,
                                        @NonNull String authorAvatarUrl,
                                        List<String> labels) {
    public static NotificationDetailedIssue of(GithubIssue issue) {
        return new NotificationDetailedIssue(issue.id().value(), issue.htmlUrl(), issue.title(), issue.repoName(), issue.description(), issue.authorLogin(),
                issue.authorAvatarUrl(), issue.labels());
    }
}
