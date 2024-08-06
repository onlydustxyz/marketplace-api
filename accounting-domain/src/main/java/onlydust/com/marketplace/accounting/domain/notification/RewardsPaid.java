package onlydust.com.marketplace.accounting.domain.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

import java.util.ArrayList;
import java.util.List;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@NotificationType("RewardsPaid")
@Builder
@NoArgsConstructor
public class RewardsPaid extends NotificationData {
    @NonNull
    @Builder.Default
    List<ShortReward> shortRewards = new ArrayList<>();

    @Override
    public NotificationCategory category() {
        return NotificationCategory.CONTRIBUTOR_REWARD;
    }
}
