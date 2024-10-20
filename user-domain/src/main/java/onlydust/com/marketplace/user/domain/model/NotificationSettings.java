package onlydust.com.marketplace.user.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
@Accessors(fluent = true)
public class NotificationSettings {

    Map<NotificationCategory, List<NotificationChannel>> channelsPerCategory;

    public static NotificationSettings defaultSettings() {
        final Map<NotificationCategory, List<NotificationChannel>> channelsPerCategory = new HashMap<>();
        for (var notificationCategory : NotificationCategory.values()) {
            channelsPerCategory.put(notificationCategory, notificationCategory.defaultChannels());
        }
        return new NotificationSettings(channelsPerCategory);
    }

    public record Project(@NonNull ProjectId projectId,
                          @NonNull Optional<Boolean> onGoodFirstIssueAdded) {
        public Project patchWith(Project other) {
            return new Project(
                    projectId,
                    other.onGoodFirstIssueAdded().isPresent() ? other.onGoodFirstIssueAdded() : onGoodFirstIssueAdded
            );
        }
    }

    public boolean hasSubscribedToMarketingEmailNotifications() {
        return Optional.ofNullable(this.channelsPerCategory.get(NotificationCategory.GLOBAL_MARKETING))
                .map(notificationChannels -> notificationChannels.stream().anyMatch(notificationChannel -> notificationChannel.equals(NotificationChannel.EMAIL)))
                .orElse(false);

    }
}
