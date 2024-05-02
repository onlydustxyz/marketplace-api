package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("BoostNodeGuardiansRewards")
@Builder
public class BoostNodeGuardiansRewards extends Event {
    Long recipientId;
    String recipientLogin;
    BigDecimal amount;
    CurrencyView.Id currencyId;
    UUID projectId;
    UUID projectLeadId;
    Long repoId;
    List<BoostedReward> boostedRewards;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoostedReward {
        UUID id;
        BigDecimal amount;
        String currencyCode;
        String projectName;
    }
}
