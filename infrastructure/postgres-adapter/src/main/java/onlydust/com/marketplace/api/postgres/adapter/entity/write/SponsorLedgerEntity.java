package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Table(name = "sponsor_ledgers", schema = "accounting")
public class SponsorLedgerEntity {
    @Id
    @NonNull
    UUID ledgerId;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false, name = "ledger_id")
    @NonNull
    SponsorAccountEntity ledger;

    @NonNull
    UUID sponsorId;

    public static SponsorLedgerEntity of(SponsorAccountEntity ledger, SponsorId sponsorId) {
        return new SponsorLedgerEntity(ledger.id, ledger, sponsorId.value());
    }
}
