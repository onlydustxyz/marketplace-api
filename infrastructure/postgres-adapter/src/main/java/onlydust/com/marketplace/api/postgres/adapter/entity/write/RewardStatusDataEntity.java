package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
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
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Accessors(fluent = true, chain = true)
public class RewardStatusDataEntity {
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
    RewardStatusEntity status;

    public static RewardStatusDataEntity of(RewardStatusData rewardStatusData) {
        return RewardStatusDataEntity.builder()
                .rewardId(rewardStatusData.rewardId().value())
                .sponsorHasEnoughFund(rewardStatusData.sponsorHasEnoughFund())
                .unlockDate(rewardStatusData.unlockDate().map(ZonedDateTime::toInstant).map(Date::from).orElse(null))
                .invoiceReceivedAt(rewardStatusData.invoiceReceivedAt().map(ZonedDateTime::toInstant).map(Date::from).orElse(null))
                .paidAt(rewardStatusData.paidAt().map(ZonedDateTime::toInstant).map(Date::from).orElse(null))
                .networks(rewardStatusData.networks().stream().map(NetworkEnumEntity::of).toArray(NetworkEnumEntity[]::new))
                .amountUsdEquivalent(rewardStatusData.usdAmount().map(ConvertedAmount::convertedAmount).map(Amount::getValue).orElse(null))
                .usdConversionRate(rewardStatusData.usdAmount().map(ConvertedAmount::conversionRate).orElse(null))
                .build();
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
