package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CurrencyViewEntity;
import onlydust.com.marketplace.project.domain.view.Money;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@Immutable
public class UserRewardStatsReadEntity {
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

    public Money toPendingMoney() {
        return new Money(this.getPendingAmount(), this.getCurrency().toView())
                .dollarsEquivalentValue(this.getPendingUsdAmount());
    }

    public Money toRewardedMoney() {
        return new Money(this.getProcessedAmount(), this.getCurrency().toView())
                .dollarsEquivalentValue(this.getProcessedUsdAmount());
    }

}
