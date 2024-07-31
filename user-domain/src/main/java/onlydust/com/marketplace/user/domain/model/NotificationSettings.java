package onlydust.com.marketplace.user.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
@Accessors(fluent = true)
public class NotificationSettings {

    Map<NotificationCategory, List<NotificationChannel>> channelsPerCategory;

    public record Project(@NonNull ProjectId projectId,
                          @NonNull Optional<Boolean> onGoodFirstIssueAdded) {
        public Project patchWith(Project other) {
            return new Project(
                    projectId,
                    other.onGoodFirstIssueAdded().isPresent() ? other.onGoodFirstIssueAdded() : onGoodFirstIssueAdded
            );
        }
    }
}
