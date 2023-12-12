package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@TypeDef(name = "currency", typeClass = CurrencyEnumEntity.class)
public class RewardStatsEntity {
    @Id
    @Type(type = "currency")
    @Enumerated(EnumType.STRING)
    CurrencyEnumEntity currency;
    BigDecimal processedAmount;
    BigDecimal processedUsdAmount;
    BigDecimal pendingAmount;
    BigDecimal pendingUsdAmount;
    Integer rewardsCount;
    Integer rewardItemsCount;
    Integer projectsCount;
}
