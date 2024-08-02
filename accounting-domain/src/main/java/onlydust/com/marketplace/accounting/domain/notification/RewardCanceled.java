package onlydust.com.marketplace.accounting.domain.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@NotificationType("RewardCanceled")
@Builder
@NoArgsConstructor(force = true)
public class RewardCanceled extends NotificationData {
    @NonNull
    ShortReward shortReward;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.CONTRIBUTOR_REWARD;
    }
}
