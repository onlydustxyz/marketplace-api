package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@IdClass(BiFinancialMonthlyStatsReadEntity.PrimaryKey.class)
public class BiFinancialMonthlyStatsReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID id;

    @Id
    @NonNull
    UUID currencyId;

    @Id
    ZonedDateTime date;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal totalDeposited;

    @NonNull
    BigDecimal totalAllocated;

    @NonNull
    BigDecimal totalGranted;

    @NonNull
    BigDecimal totalRewarded;

    @NonNull
    Integer transactionCount;

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID id;
        UUID currencyId;
        ZonedDateTime date;
    }
}
