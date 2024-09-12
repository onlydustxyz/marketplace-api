package onlydust.com.marketplace.user.domain.model;

import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.UserId;

@Value
@Accessors(fluent = true, chain = true)
public class NotificationRecipient {
    UserId id;
    String email;
    String login;
}
