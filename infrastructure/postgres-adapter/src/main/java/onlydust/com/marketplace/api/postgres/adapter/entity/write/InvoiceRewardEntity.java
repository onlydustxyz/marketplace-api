package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Accessors(fluent = true)
@Getter
public class InvoiceRewardEntity {
    @Id
    @Getter
    @NonNull
    UUID id;
    @NonNull
    String projectName;
    @NonNull
    ZonedDateTime requestedAt;
    @NonNull
    BigDecimal amount;
    @ManyToOne
    @NonNull
    CurrencyEntity currency;
    @ManyToOne
    @NonNull
    CurrencyEntity targetCurrency;
    BigDecimal targetAmount;
    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "accounting.network"
            )
    )
    @Column(columnDefinition = "accounting.network[]")
    NetworkEnumEntity[] networks;
    UUID invoiceId;

    public Invoice.Reward forInvoice() {
        return new Invoice.Reward(
                RewardId.of(id),
                requestedAt,
                projectName,
                Money.of(amount, currency.toDomain()),
                Money.of(targetAmount, targetCurrency.toDomain()),
                invoiceId == null ? null : Invoice.Id.of(invoiceId),
                Arrays.stream(networks).map(NetworkEnumEntity::toNetwork).toList()
        );
    }

    public static InvoiceRewardEntity of(Invoice.Reward reward) {
        return InvoiceRewardEntity.builder()
                .id(reward.id().value())
                .projectName(reward.projectName())
                .requestedAt(reward.createdAt())
                .amount(reward.amount().getValue())
                .currency(CurrencyEntity.of(reward.amount().getCurrency()))
                .targetCurrency(CurrencyEntity.of(reward.target().getCurrency()))
                .targetAmount(reward.target().getValue())
                .invoiceId(reward.invoiceId() == null ? null : reward.invoiceId().value())
                .networks(reward.networks().stream().map(NetworkEnumEntity::of).toArray(NetworkEnumEntity[]::new))
                .build();
    }
}
