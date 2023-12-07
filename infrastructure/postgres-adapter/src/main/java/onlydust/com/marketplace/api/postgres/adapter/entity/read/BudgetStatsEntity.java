package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
public class BudgetStatsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    BigDecimal spentAmount;
    BigDecimal spentUsdAmount;
    BigDecimal remainingAmount;
    BigDecimal remainingUsdAmount;
    Integer rewardsCount;
    Integer rewardItemsCount;
    Integer rewardRecipientsCount;
}
