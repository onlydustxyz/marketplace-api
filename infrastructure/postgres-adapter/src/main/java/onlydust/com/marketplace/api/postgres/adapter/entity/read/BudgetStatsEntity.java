package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
public class BudgetStatsEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    CurrencyEnumEntity currency;
    BigDecimal spentAmount;
    BigDecimal spentUsdAmount;
    BigDecimal remainingAmount;
    BigDecimal remainingUsdAmount;
    Integer rewardsCount;
    Integer rewardItemsCount;
    Integer rewardRecipientsCount;
}
