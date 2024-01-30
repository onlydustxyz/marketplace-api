package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.ContributorId;
import onlydust.com.marketplace.accounting.domain.model.Ledger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Table(name = "contributor_ledgers", schema = "sandbox")
@IdClass(ContributorLedgerEntity.PrimaryKey.class)
public class ContributorLedgerEntity {
    @Id
    @NonNull UUID ledgerId;

    @Id
    @NonNull Long githubUserId;

    public static ContributorLedgerEntity of(Ledger.Id ledgerId, ContributorId contributorId) {
        return new ContributorLedgerEntity(ledgerId.value(), contributorId.value());
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        private UUID ledgerId;
        private Long githubUserId;
    }
}
