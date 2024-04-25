package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
public class BoEarningsViewEntity {
    @Id
    Integer id;

    @JdbcTypeCode(SqlTypes.JSON)
    List<EarningsPerCurrencyViewEntity> earningsPerCurrency;

    public EarningsView toView() {
        return new EarningsView(earningsPerCurrency != null ? earningsPerCurrency.stream().map(EarningsPerCurrencyViewEntity::toDomain).toList() : List.of());
    }

    public record EarningsPerCurrencyViewEntity(@NonNull Long rewardCount,
                                                @NonNull BigDecimal amount,
                                                @NonNull CurrencyViewEntity currency,
                                                BigDecimal dollarsEquivalent) {
        public EarningsView.EarningsPerCurrency toDomain() {
            return new EarningsView.EarningsPerCurrency(new TotalMoneyView(amount, currency.toDomain(), dollarsEquivalent), rewardCount);
        }
    }

    public record CurrencyViewEntity(@NonNull UUID id,
                                     @NonNull String name,
                                     @NonNull String code,
                                     @NonNull Integer decimals,
                                     URI logoUrl
    ) {
        CurrencyView toDomain() {
            return CurrencyView.builder()
                    .id(CurrencyView.Id.of(id))
                    .name(name)
                    .code(code)
                    .decimals(decimals)
                    .logoUrl(logoUrl)
                    .build();
        }
    }
}
