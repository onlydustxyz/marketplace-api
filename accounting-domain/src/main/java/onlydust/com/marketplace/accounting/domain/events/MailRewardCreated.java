package onlydust.com.marketplace.accounting.domain.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.UUID;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@EventType("RewardCreated")
public class MailRewardCreated extends Event {
    @NonNull
    String email;
    @NonNull
    Integer contributionsNumber;
    @NonNull
    String sentByGithubLogin;
    @NonNull
    String recipientGithubLogin;
    @NonNull
    ShortReward shortReward;
    UUID recipientId;
}