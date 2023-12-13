package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
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
@TypeDef(name = "currency", typeClass = CurrencyEnumEntity.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class RewardStatsEntity {
    @Id
    @Type(type = "currency")
    @Enumerated(EnumType.STRING)
    CurrencyEnumEntity currency;
    BigDecimal processedAmount;
    BigDecimal processedUsdAmount;
    BigDecimal pendingAmount;
    BigDecimal pendingUsdAmount;
    @Type(type = "jsonb")
    Set<UUID> rewardIds;
    @Type(type = "jsonb")
    Set<String> rewardItemIds;
    @Type(type = "jsonb")
    Set<UUID> projectIds;
}
