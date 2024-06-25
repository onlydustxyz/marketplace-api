package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
