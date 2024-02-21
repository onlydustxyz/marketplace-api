package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
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
    @NonNull UUID id;
    @NonNull String projectName;
    @NonNull ZonedDateTime requestedAt;
    @NonNull BigDecimal amount;
    @ManyToOne
    @NonNull CurrencyEntity currency;
    @ManyToOne
    @NonNull CurrencyEntity baseCurrency;
    @NonNull BigDecimal baseAmount;

    public Invoice.Reward forInvoice() {
        return new Invoice.Reward(
                RewardId.of(id),
                requestedAt,
                projectName,
                Money.of(amount, currency.toDomain()),
                Money.of(baseAmount, baseCurrency.toDomain())
        );
    }

    public static InvoiceRewardEntity of(Invoice.Reward reward) {
        return InvoiceRewardEntity.builder()
                .id(reward.id().value())
                .projectName(reward.projectName())
                .requestedAt(reward.createdAt())
                .amount(reward.amount().getValue())
                .currency(CurrencyEntity.of(reward.amount().getCurrency()))
                .baseCurrency(CurrencyEntity.of(reward.base().getCurrency()))
                .baseAmount(reward.base().getValue())
                .build();
    }
}
