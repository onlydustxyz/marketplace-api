package onlydust.com.marketplace.project.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NotificationType("CommitteeApplicationSuccessfullyCreated")
@Builder
public class CommitteeApplicationCreated extends NotificationData {
    @NonNull
    String projectName;
    @NonNull
    ProjectId projectId;
    @NonNull
    String committeeName;
    @NonNull
    Committee.Id committeeId;
    @NonNull
    ZonedDateTime applicationEndDate;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.MAINTAINER_PROJECT_PROGRAM;
    }
}
