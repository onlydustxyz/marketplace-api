package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.ContributorId;

import javax.persistence.*;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Table(name = "contributor_ledgers", schema = "sandbox")
public class ContributorLedgerEntity {
    @Id
    @NonNull UUID ledgerId;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false, name = "ledger_id")
    @NonNull LedgerEntity ledger;

    @NonNull Long githubUserId;

    public static ContributorLedgerEntity of(LedgerEntity ledger, ContributorId contributorId) {
        return new ContributorLedgerEntity(ledger.id, ledger, contributorId.value());
    }
}
