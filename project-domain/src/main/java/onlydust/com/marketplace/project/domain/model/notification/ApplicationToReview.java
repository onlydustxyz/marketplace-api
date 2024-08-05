package onlydust.com.marketplace.project.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NotificationType("ProjectApplicationToReview")
@Builder
public class ApplicationToReview extends NotificationData {
    @NonNull
    NotificationProject project;
    @NonNull
    NotificationIssue issue;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.MAINTAINER_PROJECT_CONTRIBUTOR;
    }
}
