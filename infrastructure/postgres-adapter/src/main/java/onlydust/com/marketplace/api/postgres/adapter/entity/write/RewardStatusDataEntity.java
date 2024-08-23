package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Entity
@Data
@Table(name = "reward_status_data", schema = "accounting")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true, chain = true)
public class RewardStatusDataEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
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
    @JoinColumn(name = "rewardId", referencedColumnName = "rewardId", insertable = false, updatable = false)
    RewardStatusEntity status;

    public static RewardStatusDataEntity of(RewardStatusData rewardStatusData) {
        return new RewardStatusDataEntity()
                .rewardId(rewardStatusData.rewardId().value())
                .sponsorHasEnoughFund(rewardStatusData.sponsorHasEnoughFund())
                .unlockDate(rewardStatusData.unlockDate().map(ZonedDateTime::toInstant).map(Date::from).orElse(null))
                .invoiceReceivedAt(rewardStatusData.invoiceReceivedAt().map(ZonedDateTime::toInstant).map(Date::from).orElse(null))
                .paidAt(rewardStatusData.paidAt().map(ZonedDateTime::toInstant).map(Date::from).orElse(null))
                .networks(rewardStatusData.networks().stream().map(NetworkEnumEntity::of).toArray(NetworkEnumEntity[]::new))
                .amountUsdEquivalent(rewardStatusData.usdAmount().map(ConvertedAmount::convertedAmount).map(Amount::getValue).orElse(null))
                .usdConversionRate(rewardStatusData.usdAmount().map(ConvertedAmount::conversionRate).orElse(null));
    }

    public RewardStatusData toRewardStatus() {
        return new RewardStatusData(RewardId.of(rewardId))
                .sponsorHasEnoughFund(Boolean.TRUE.equals(sponsorHasEnoughFund))
                .unlockDate(unlockDate == null ? null : ZonedDateTime.ofInstant(unlockDate.toInstant(), ZoneOffset.UTC))
                .invoiceReceivedAt(invoiceReceivedAt == null ? null : ZonedDateTime.ofInstant(invoiceReceivedAt.toInstant(), ZoneOffset.UTC))
                .paidAt(paidAt == null ? null : ZonedDateTime.ofInstant(paidAt.toInstant(), ZoneOffset.UTC))
                .withAdditionalNetworks(Arrays.stream(networks).map(NetworkEnumEntity::toNetwork).collect(Collectors.toSet()))
                .usdAmount(isNull(usdConversionRate) || isNull(amountUsdEquivalent) ? null : new ConvertedAmount(Amount.of(amountUsdEquivalent),
                        usdConversionRate));
    }
}
