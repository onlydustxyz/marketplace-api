package onlydust.com.marketplace.project.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NotificationType("ApplicationAccepted")
@Builder
public class ApplicationAccepted extends NotificationData {
    @NonNull
    Project project;
    @NonNull
    Issue issue;

    public record Project(@NonNull UUID id,
                          @NonNull String slug,
                          @NonNull String name) {
    }

    public record Issue(@NonNull Long id,
                        @NonNull String htmlUrl,
                        @NonNull String title,
                        @NonNull String repoName,
                        String description) {
    }

    @Override
    public NotificationCategory category() {
        return NotificationCategory.CONTRIBUTOR_PROJECT;
    }
}
