package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Table(name = "account_book_transactions", schema = "accounting")
@IdClass(ProgramTransactionMonthlyStatReadEntity.PrimaryKey.class)
public class ProgramTransactionMonthlyStatReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID programId;

    @Id
    @NonNull
    UUID currencyId;

    @Id
    Date date;

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

    @NonNull
    Integer transactionCount;

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID programId;
        UUID currencyId;
        Date date;
    }
}