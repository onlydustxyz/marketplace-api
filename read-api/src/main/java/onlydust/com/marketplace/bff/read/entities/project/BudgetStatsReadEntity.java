package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CurrencyViewEntity;
import onlydust.com.marketplace.project.domain.view.Money;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@Immutable
public class BudgetStatsReadEntity {
    @Id
    UUID currencyId;
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyViewEntity currency;
    BigDecimal spentAmount;
    BigDecimal spentUsdAmount;
    BigDecimal remainingAmount;
    BigDecimal remainingUsdAmount;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<UUID> rewardIds;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<Set<String>> rewardItemIds;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<Integer> rewardRecipientIds;

    public Money toRemainingMoney() {
        return new Money(this.getRemainingAmount(), this.getCurrency().toView())
                .dollarsEquivalentValue(this.getRemainingUsdAmount());
    }

    public Money toSpentMoney() {
        return new Money(this.getSpentAmount(), this.getCurrency().toView())
                .dollarsEquivalentValue(this.getSpentUsdAmount());
    }


}
