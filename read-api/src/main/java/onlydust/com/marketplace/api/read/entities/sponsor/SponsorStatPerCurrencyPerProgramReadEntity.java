package onlydust.com.marketplace.api.read.entities.sponsor;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStat;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "sponsor_stats_per_currency_per_program", schema = "bi")
@IdClass(SponsorStatPerCurrencyPerProgramReadEntity.PrimaryKey.class)
public class SponsorStatPerCurrencyPerProgramReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID sponsorId;

    @Id
    @NonNull
    UUID currencyId;

    @Id
    @NonNull
    UUID programId;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal totalAllocated;

    @NonNull
    BigDecimal totalGranted;

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID sponsorId;
        UUID currencyId;
        UUID programId;
    }
}
