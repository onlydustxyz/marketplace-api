package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentShortView;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Immutable
public class PaymentShortQueryEntity {

    @Id
    UUID id;

    Date createdAt;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "batch_payment_status")
    Payment.Status status;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    NetworkEnumEntity network;

    Long rewardCount;

    @JdbcTypeCode(SqlTypes.JSON)
    List<TotalMoneyViewEntity> totalsPerCurrency;

    public BatchPaymentShortView toDomain() {
        return BatchPaymentShortView.builder()
                .id(Payment.Id.of(id))
                .createdAt(ZonedDateTime.ofInstant(createdAt.toInstant(), ZoneOffset.UTC))
                .status(Payment.Status.valueOf(status.name()))
                .network(isNull(network) ? null : network.toNetwork())
                .rewardCount(rewardCount)
                .totalsPerCurrency(isNull(totalsPerCurrency) ? null : totalsPerCurrency.stream().map(TotalMoneyViewEntity::toDomain).toList())
                .build();
    }

    public record TotalMoneyViewEntity(@NonNull BigDecimal amount,
                                       @NonNull CurrencyViewEntity currency,
                                       BigDecimal dollarsEquivalent) {
        TotalMoneyView toDomain() {
            return new TotalMoneyView(amount, currency.toDomain(), dollarsEquivalent);
        }
    }

    public record CurrencyViewEntity(@NonNull UUID id,
                                     @NonNull String name,
                                     @NonNull String code,
                                     @NonNull Integer decimals,
                                     BigDecimal latestUsdQuote,
                                     URI logoUrl
    ) {
        CurrencyView toDomain() {
            return CurrencyView.builder()
                    .id(CurrencyView.Id.of(id))
                    .name(name)
                    .code(code)
                    .decimals(decimals)
                    .latestUsdQuote(latestUsdQuote)
                    .logoUrl(logoUrl)
                    .build();
        }
    }
}