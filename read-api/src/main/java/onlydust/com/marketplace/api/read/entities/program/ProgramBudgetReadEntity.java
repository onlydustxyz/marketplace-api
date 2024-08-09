package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.hibernate.annotations.Immutable;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@IdClass(ProgramBudgetReadEntity.PrimaryKey.class)
public class ProgramBudgetReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long index;

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID programId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID currencyId;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal amount;

    public Money toMoney() {
        final var usdQuote = currency.latestUsdQuote() == null ? null : currency.latestUsdQuote().getPrice();

        return new Money()
                .amount(amount)
                .currency(currency.toShortResponse())
                .prettyAmount(pretty(amount, currency.decimals(), usdQuote))
                .usdEquivalent(prettyUsd(usdAmount()))
                .usdConversionRate(usdQuote);
    }

    public @Nullable BigDecimal usdAmount() {
        final var usdQuote = currency.latestUsdQuote() == null ? null : currency.latestUsdQuote().getPrice();
        return usdQuote == null ? null : usdQuote.multiply(amount);
    }


    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        Long index;
        UUID programId;
        UUID currencyId;
    }
}
