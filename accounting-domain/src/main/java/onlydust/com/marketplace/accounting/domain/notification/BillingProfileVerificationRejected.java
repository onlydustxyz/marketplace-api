package onlydust.com.marketplace.accounting.domain.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@NotificationType("BillingProfileVerificationRejected")
@Builder
@NoArgsConstructor(force = true)
public class BillingProfileVerificationRejected extends NotificationData {
    @NonNull
    BillingProfile.Id billingProfileId;
    @NonNull
    String billingProfileName;
    @NonNull
    String rejectionReason;

    @Override
    public NotificationCategory category() {
        return NotificationCategory.GLOBAL_BILLING_PROFILE;
    }
}
