package onlydust.com.marketplace.accounting.domain.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@EventType("InvoiceRejected")
public class InvoiceRejected extends Event {
    @NonNull
    String billingProfileAdminEmail;
    @NonNull
    Long rewardCount;
    @NonNull
    String billingProfileAdminGithubLogin;
    String billingProfileAdminFirstName;
    @NonNull
    UUID billingProfileAdminId;
    @NonNull
    String invoiceName;
    @NonNull
    List<ShortReward> rewards;
    String rejectionReason;
}
