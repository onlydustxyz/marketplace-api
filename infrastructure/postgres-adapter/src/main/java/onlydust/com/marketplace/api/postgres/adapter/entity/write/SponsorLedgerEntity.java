package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

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
@Table(name = "sponsor_ledgers", schema = "sandbox")
@IdClass(SponsorLedgerEntity.PrimaryKey.class)
public class SponsorLedgerEntity {
    @Id
    @NonNull UUID ledgerId;

    @Id
    @NonNull UUID sponsorId;

    public static SponsorLedgerEntity of(Ledger.Id ledgerId, SponsorId sponsorId) {
        return new SponsorLedgerEntity(ledgerId.value(), sponsorId.value());
    }

    @EqualsAndHashCode
    @ToString
    public static class PrimaryKey implements Serializable {
        private UUID ledgerId;
        private UUID sponsorId;
    }
}
