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
@IdClass(SponsorTransactionMonthlyStatReadEntity.PrimaryKey.class)
public class SponsorTransactionMonthlyStatReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID sponsorId;

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
    BigDecimal totalAvailable;

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
        UUID sponsorId;
        UUID currencyId;
        ZonedDateTime date;
    }
}
