package onlydust.com.marketplace.project.domain.model.notification.dto;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.UserId;

public record NotificationUser(UserId id,
                               @NonNull Long githubId,
                               @NonNull String login) {
}
