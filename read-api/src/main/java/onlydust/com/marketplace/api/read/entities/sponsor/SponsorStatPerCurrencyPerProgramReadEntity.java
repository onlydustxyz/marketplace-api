package onlydust.com.marketplace.api.read.entities.sponsor;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.MoneyResponse;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStat;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;

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
    @JoinColumn(name = "programId", insertable = false, updatable = false)
    ProgramReadEntity program;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal totalAllocated;

    @NonNull
    BigDecimal totalGranted;

    public MoneyResponse toBoMoneyResponse(Function<SponsorStatPerCurrencyPerProgramReadEntity, BigDecimal> amount) {
        return new MoneyResponse()
                .amount(amount.apply(this))
                .currency(currency().toBoShortResponse());
    }

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID sponsorId;
        UUID currencyId;
        UUID programId;
    }
}
