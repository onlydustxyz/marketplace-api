package onlydust.com.marketplace.accounting.domain.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@EventType("TrackingRewardCreated")
public class TrackingRewardCreated extends Event {
    @NonNull
    UUID projectId;
    @NonNull
    Long githubRecipientId;
    UUID recipientId;
    @NonNull
    Long githubSenderId;
    @NonNull
    UUID senderId;
    @NonNull
    String currencyCode;
    @NonNull
    BigDecimal amount;
    BigDecimal dollarsEquivalent;
    @NonNull
    Integer contributionsCount;
    @NonNull
    RewardId id;
}
