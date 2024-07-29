package onlydust.com.marketplace.user.domain.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.notification.Notification;

/**
 * Represents the recipient (user) of a notification.
 */
@Value
@Accessors(fluent = true, chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class SendableNotification extends Notification {
    String recipientEmail;
    String recipientLogin;
}
