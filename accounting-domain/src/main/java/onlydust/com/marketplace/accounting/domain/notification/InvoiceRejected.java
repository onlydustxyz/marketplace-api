package onlydust.com.marketplace.accounting.domain.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

import java.util.List;
import java.util.UUID;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@NotificationType("InvoiceRejected")
@Builder
@NoArgsConstructor(force = true)
public class InvoiceRejected extends NotificationData {
    @NonNull
    String invoiceName;
    @NonNull
    List<ShortReward> rewards;
    String rejectionReason;
    @NonNull
    UUID billingProfileId;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.CONTRIBUTOR_REWARD;
    }
}
