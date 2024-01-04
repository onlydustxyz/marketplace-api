package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BudgetStatsEntity {

  @Id
  @Type(type = "currency")
  @Enumerated(EnumType.STRING)
  CurrencyEnumEntity currency;
  BigDecimal spentAmount;
  BigDecimal spentUsdAmount;
  BigDecimal remainingAmount;
  BigDecimal remainingUsdAmount;
  @Type(type = "jsonb")
  Set<UUID> rewardIds;
  @Type(type = "jsonb")
  Set<String> rewardItemIds;
  @Type(type = "jsonb")
  Set<Integer> rewardRecipientIds;
}
