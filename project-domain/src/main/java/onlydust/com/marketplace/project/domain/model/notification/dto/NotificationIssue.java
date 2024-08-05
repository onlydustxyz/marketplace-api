package onlydust.com.marketplace.project.domain.model.notification.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

public record NotificationIssue(@NonNull Long id,
                                @NonNull String htmlUrl,
                                @NonNull String title,
                                @NonNull String repoName,
                                String description) {
    public static NotificationIssue of(GithubIssue issue) {
        return new NotificationIssue(issue.id().value(), issue.htmlUrl(), issue.title(), issue.repoName(), issue.description());
    }
}
