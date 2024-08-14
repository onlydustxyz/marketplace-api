package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "program_stats_per_currency", schema = "bi")
@IdClass(ProgramStatPerCurrencyReadEntity.PrimaryKey.class)
public class ProgramStatPerCurrencyReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID programId;

    @Id
    @NonNull
    UUID currencyId;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "programId", insertable = false, updatable = false)
    ProgramReadEntity program;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal totalAvailable;

    @NonNull
    BigDecimal totalGranted;

    @NonNull
    BigDecimal totalRewarded;

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID programId;
        UUID currencyId;
    }
}
