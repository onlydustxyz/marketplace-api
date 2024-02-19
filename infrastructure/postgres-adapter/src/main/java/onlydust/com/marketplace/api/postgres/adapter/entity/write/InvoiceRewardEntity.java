package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
public class InvoiceRewardEntity {
    @Id
    @Getter
    @NonNull UUID id;
    @ManyToOne
    @JoinColumn(referencedColumnName = "project_id", name = "project_id")
    @NonNull ProjectEntity project;
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
                ProjectId.of(project.getId()),
                project.getName(),
                Money.of(amount, currency.toDomain()),
                Money.of(baseAmount, baseCurrency.toDomain())
        );
    }

    public static InvoiceRewardEntity of(Invoice.Reward reward) {
        return InvoiceRewardEntity.builder()
                .id(reward.id().value())
                .project(ProjectEntity.builder().id(reward.projectId().value()).name(reward.projectName()).build())
                .requestedAt(reward.createdAt())
                .amount(reward.amount().getValue())
                .currency(CurrencyEntity.of(reward.amount().getCurrency()))
                .baseCurrency(CurrencyEntity.of(reward.base().getCurrency()))
                .baseAmount(reward.base().getValue())
                .build();
    }
}
