package onlydust.com.marketplace.project.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@NotificationType("FundsUngrantedFromProject")
@Builder
@NoArgsConstructor(force = true)
public class FundsUngrantedFromProject extends NotificationData {
    @NonNull
    ProgramId programId;
    @NonNull
    ProjectId projectId;
    @NonNull
    BigDecimal amount;
    @NonNull
    UUID currencyId;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.PROGRAM_LEAD;
    }
}
