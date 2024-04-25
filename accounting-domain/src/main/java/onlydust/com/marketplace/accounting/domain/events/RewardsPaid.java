package onlydust.com.marketplace.accounting.domain.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.List;
import java.util.UUID;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@EventType("RewardsPaid")
public class RewardsPaid extends Event {
    @NonNull
    String recipientEmail;
    @NonNull
    String recipientGithubLogin;
    UUID recipientId;
    @NonNull
    List<ShortReward> shortRewards;
}
