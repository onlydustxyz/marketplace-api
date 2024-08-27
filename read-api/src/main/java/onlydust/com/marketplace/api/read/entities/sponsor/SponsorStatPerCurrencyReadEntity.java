package onlydust.com.marketplace.api.read.entities.sponsor;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.SponsorBudgetResponse;
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
@Table(name = "sponsor_stats_per_currency", schema = "bi")
@IdClass(SponsorStatPerCurrencyReadEntity.PrimaryKey.class)
public class SponsorStatPerCurrencyReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID sponsorId;

    @Id
    @NonNull
    UUID currencyId;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal initialAllowance;

    @NonNull
    BigDecimal totalGranted;

    @NonNull
    BigDecimal totalAllocated;

    @NonNull
    BigDecimal totalRewarded;

    @NonNull
    BigDecimal totalPaid;

    @NonNull
    BigDecimal initialBalance;

    @NonNull
    BigDecimal totalSpent;

    public @NonNull BigDecimal totalAvailable() {
        return initialAllowance.subtract(totalAllocated);
    }

    public @NonNull BigDecimal debt() {
        return initialAllowance.subtract(initialBalance());
    }

    public @NonNull BigDecimal totalAwaitingPayment() {
        return totalRewarded.subtract(totalPaid);
    }

    public @NonNull BigDecimal currentBalance() {
        return initialBalance().subtract(totalSpent);
    }

    public SponsorBudgetResponse toSponsorBudgetResponse() {
        return new SponsorBudgetResponse()
                .currency(currency.toBoShortResponse())
                .initialBalance(initialBalance)
                .currentBalance(currentBalance())
                .initialAllowance(initialAllowance)
                .currentAllowance(totalAvailable())
                .debt(debt())
                .awaitingPaymentAmount(totalAwaitingPayment())
                ;
    }

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID sponsorId;
        UUID currencyId;
    }
}
