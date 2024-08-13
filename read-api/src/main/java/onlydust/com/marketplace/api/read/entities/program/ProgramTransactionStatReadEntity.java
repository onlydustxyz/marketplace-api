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
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "account_book_transactions", schema = "accounting")
@IdClass(ProgramTransactionStatReadEntity.PrimaryKey.class)
public class ProgramTransactionStatReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID programId;

    @Id
    @NonNull
    UUID currencyId;

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
