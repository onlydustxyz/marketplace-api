package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import javax.persistence.*;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Table(name = "reward_ledgers", schema = "accounting")
public class RewardLedgerEntity {
    @Id
    @NonNull UUID ledgerId;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false, name = "ledger_id")
    @NonNull LedgerEntity ledger;

    @NonNull UUID rewardId;

    public static RewardLedgerEntity of(LedgerEntity ledger, RewardId rewardId) {
        return new RewardLedgerEntity(ledger.id, ledger, rewardId.value());
    }
}
