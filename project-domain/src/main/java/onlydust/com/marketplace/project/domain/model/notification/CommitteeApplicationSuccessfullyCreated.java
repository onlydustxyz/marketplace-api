package onlydust.com.marketplace.project.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NotificationType("CommitteeApplicationSuccessfullyCreated")
@Builder
public class CommitteeApplicationSuccessfullyCreated extends NotificationData {
    @NonNull
    String projectName;
    @NonNull
    UUID projectId;
    @NonNull
    String committeeName;
    @NonNull
    UUID committeeId;
    @NonNull
    ZonedDateTime applicationEndDate;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.MAINTAINER_PROJECT_PROGRAM;
    }
}
