package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
public class BudgetStatsViewEntity {
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
}
