package onlydust.com.marketplace.user.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("UserSignedUp")
public class UserSignedUp extends Event {
    UUID userId;
    Long githubUserId;
    String login;
    Date signedUpAt;
}
