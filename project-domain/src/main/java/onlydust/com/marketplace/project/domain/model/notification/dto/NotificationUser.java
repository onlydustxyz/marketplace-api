package onlydust.com.marketplace.project.domain.model.notification.dto;

import lombok.NonNull;

import java.util.UUID;

public record NotificationUser(UUID id,
                               @NonNull Long githubId,
                               @NonNull String login) {
}
