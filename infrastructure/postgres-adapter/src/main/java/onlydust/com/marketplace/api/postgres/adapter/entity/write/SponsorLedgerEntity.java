package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.project.domain.model.Sponsor;

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

    public static SponsorLedgerEntity of(SponsorAccountEntity ledger, Sponsor.Id sponsorId) {
        return new SponsorLedgerEntity(ledger.id, ledger, sponsorId.value());
    }
}
