package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "reward_ledgers", schema = "accounting")
public class RewardLedgerEntity {
    @Id
    @NonNull
    UUID ledgerId;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false, name = "ledger_id")
    @NonNull
    SponsorAccountEntity ledger;

    @NonNull
    UUID rewardId;

    public static RewardLedgerEntity of(SponsorAccountEntity ledger, RewardId rewardId) {
        return new RewardLedgerEntity(ledger.id, ledger, rewardId.value());
    }
}
