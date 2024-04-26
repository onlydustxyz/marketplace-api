package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class BoEarningsViewEntity {
    @Id
    @NonNull
    @Column(name = "currency_id")
    UUID currencyId;
    @NonNull Long rewardCount;
    @NonNull BigDecimal totalAmount;
    BigDecimal totalDollarsEquivalent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", insertable = false, updatable = false)
    CurrencyEntity currency;

    public EarningsView.EarningsPerCurrency toDomain() {
        return new EarningsView.EarningsPerCurrency(new TotalMoneyView(totalAmount, currency.toView(), totalDollarsEquivalent), rewardCount);
    }
}
