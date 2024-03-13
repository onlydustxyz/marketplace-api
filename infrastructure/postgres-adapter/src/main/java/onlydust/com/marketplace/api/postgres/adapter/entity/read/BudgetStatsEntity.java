package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BudgetStatsEntity {
    @Id
    UUID currencyId;
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyEntity currency;
    BigDecimal spentAmount;
    BigDecimal spentUsdAmount;
    BigDecimal remainingAmount;
    BigDecimal remainingUsdAmount;
    @Type(type = "jsonb")
    Set<UUID> rewardIds;
    @Type(type = "jsonb")
    Set<Set<String>> rewardItemIds;
    @Type(type = "jsonb")
    Set<Integer> rewardRecipientIds;
}
