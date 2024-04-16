package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentShortView;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@TypeDef(name = "batch_payment_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class PaymentShortViewEntity {

    @Id
    UUID id;

    Date createdAt;

    @Type(type = "batch_payment_status")
    @Enumerated(EnumType.STRING)
    BatchPaymentEntity.Status status;

    @Enumerated(EnumType.STRING)
    @Type(type = "network")
    NetworkEnumEntity network;

    Long rewardCount;

    @Type(type = "jsonb")
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
