package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
public class InvoiceRewardEntity {
    @Id
    @NonNull UUID id;
    @ManyToOne
    @JoinColumn(referencedColumnName = "project_id", name = "project_id")
    @NonNull ProjectEntity project;
    @NonNull ZonedDateTime requestedAt;
    @NonNull BigDecimal amount;
    @ManyToOne
    @NonNull CurrencyEntity currency;
    @ManyToOne
    @NonNull CurrencyEntity base;
    @NonNull BigDecimal conversionRate;

    public InvoicePreview.Reward forInvoicePreview() {
        return new InvoicePreview.Reward(
                RewardId.of(id),
                requestedAt,
                project.getName(),
                Money.of(amount, currency.toDomain()),
                Money.of(amount.multiply(conversionRate), base.toDomain())
        );
    }
}
