package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class RewardStatsEntity {
    @Id
    @Column(name = "currency_id")
    UUID currencyId;
    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id", insertable = false, updatable = false)
    CurrencyEntity currency;
    BigDecimal processedAmount;
    BigDecimal processedUsdAmount;
    BigDecimal pendingAmount;
    BigDecimal pendingUsdAmount;
    Integer pendingRequestCount;
    @Type(type = "jsonb")
    Set<UUID> rewardIds;
    @Type(type = "jsonb")
    Set<Set<String>> rewardItemIds;
    @Type(type = "jsonb")
    Set<UUID> projectIds;
}
