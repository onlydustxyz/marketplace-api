package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
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
public class RewardStatsViewEntity {
    @Id
    @Column(name = "currency_id")
    UUID currencyId;
    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id", insertable = false, updatable = false)
    CurrencyViewEntity currency;
    BigDecimal processedAmount;
    BigDecimal processedUsdAmount;
    BigDecimal pendingAmount;
    BigDecimal pendingUsdAmount;
    Integer pendingRequestCount;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<UUID> rewardIds;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<Set<String>> rewardItemIds;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<UUID> projectIds;
}
