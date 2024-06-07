package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "reward_status_data", schema = "accounting")
@NoArgsConstructor(force = true)
@EqualsAndHashCode
@Accessors(fluent = true, chain = true)
@Immutable
public class RewardStatusDataViewEntity {
    @Id
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", referencedColumnName = "reward_id", insertable = false, updatable = false)
    RewardStatusViewEntity status;
}
