package onlydust.com.marketplace.accounting.domain.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.math.BigDecimal;
import java.util.List;

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
    String invoiceName;
    @NonNull
    List<ShortReward> rewards;
    String rejectionReason;

    @Data
    @Builder
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class ShortReward {
        RewardId id;
        String projectName;
        String currencyCode;
        BigDecimal amount;
        BigDecimal dollarsEquivalent;
    }
}
