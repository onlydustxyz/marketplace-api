package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import onlydust.com.marketplace.kernel.model.*;

import java.math.BigDecimal;
import java.util.List;

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
    ProjectId projectId;
    UserId projectLeadId;
    Long repoId;
    List<BoostedReward> boostedRewards;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoostedReward {
        RewardId id;
        BigDecimal amount;
        String currencyCode;
        String projectName;
    }
}
