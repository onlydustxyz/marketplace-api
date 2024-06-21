package onlydust.com.marketplace.api.read.entities.reward;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Value
@Table(name = "reward_status_data", schema = "accounting")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true, chain = true)
@Immutable
public class RewardStatusDataReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID rewardId;

    Boolean sponsorHasEnoughFund;
    Date unlockDate;
    Date invoiceReceivedAt;
    Date paidAt;
    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "accounting.network"
            )
    )
    @Column(name = "networks", columnDefinition = "accounting.network[]")
    NetworkEnumEntity[] networks;
    BigDecimal amountUsdEquivalent;
    BigDecimal usdConversionRate;
}
