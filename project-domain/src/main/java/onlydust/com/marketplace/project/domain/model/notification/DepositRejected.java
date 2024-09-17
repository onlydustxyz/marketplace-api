package onlydust.com.marketplace.project.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@NotificationType("DepositRejected")
@Builder
@NoArgsConstructor(force = true)
public class DepositRejected extends NotificationData {
    @NonNull
    UUID depositId;
    @NonNull
    SponsorId sponsorId;
    @NonNull
    BigDecimal amount;
    @NonNull
    UUID currencyId;
    @NonNull
    ZonedDateTime timestamp;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.SPONSOR_LEAD;
    }
}
