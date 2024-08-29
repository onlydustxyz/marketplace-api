package onlydust.com.marketplace.project.domain.model.notification;


import lombok.*;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationDetailedIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NotificationType("GoodFirstIssueCreated")
@Builder
public class GoodFirstIssueCreated extends NotificationData {
    @NonNull
    NotificationProject project;
    @NonNull
    NotificationDetailedIssue issue;

    public NotificationCategory category() {
        return NotificationCategory.CONTRIBUTOR_PROJECT;
    }
}
