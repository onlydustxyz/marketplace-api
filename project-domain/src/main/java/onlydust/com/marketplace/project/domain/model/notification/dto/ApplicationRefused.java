package onlydust.com.marketplace.project.domain.model.notification.dto;

import lombok.*;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NotificationType("ApplicationRefused")
@Builder
public class ApplicationRefused extends NotificationData {
    @NonNull
    NotificationProject project;
    @NonNull
    NotificationIssue issue;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.CONTRIBUTOR_PROJECT;
    }
}
