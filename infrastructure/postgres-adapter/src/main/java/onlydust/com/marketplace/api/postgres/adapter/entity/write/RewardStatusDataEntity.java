package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Data
@Table(name = "reward_status_data", schema = "accounting")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Accessors(fluent = true, chain = true)
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
@TypeDef(
        name = "accounting.network[]",
        typeClass = EnumArrayType.class,
        defaultForType = NetworkEnumEntity[].class,
        parameters = {
                @Parameter(
                        name = AbstractArrayType.SQL_ARRAY_TYPE,
                        value = "accounting.network"
                )
        }
)
public class RewardStatusDataEntity {
    @Id
    @NonNull UUID rewardId;

    Boolean sponsorHasEnoughFund;
    Date unlockDate;
    Date invoiceReceivedAt;
    Date paidAt;
    @Type(type = "accounting.network[]")
    @Column(columnDefinition = "accounting.network[]")
    NetworkEnumEntity[] networks;
    BigDecimal amountUsdEquivalent;

    public static RewardStatusDataEntity of(RewardStatusData rewardStatusData) {
        return RewardStatusDataEntity.builder()
                .rewardId(rewardStatusData.rewardId().value())
                .sponsorHasEnoughFund(rewardStatusData.sponsorHasEnoughFund())
                .unlockDate(rewardStatusData.unlockDate().map(ChronoZonedDateTime::toInstant).map(Date::from).orElse(null))
                .invoiceReceivedAt(rewardStatusData.invoiceReceivedAt().map(ChronoZonedDateTime::toInstant).map(Date::from).orElse(null))
                .paidAt(rewardStatusData.paidAt().map(ChronoZonedDateTime::toInstant).map(Date::from).orElse(null))
                .networks(rewardStatusData.networks().stream().map(NetworkEnumEntity::of).toArray(NetworkEnumEntity[]::new))
                .amountUsdEquivalent(rewardStatusData.amountUsdEquivalent().orElse(null))
                .build();
    }

    public RewardStatusData toRewardStatus() {
        return new RewardStatusData(RewardId.of(rewardId))
                .sponsorHasEnoughFund(Boolean.TRUE.equals(sponsorHasEnoughFund))
                .unlockDate(unlockDate == null ? null : ZonedDateTime.ofInstant(unlockDate.toInstant(), ZoneOffset.UTC))
                .invoiceReceivedAt(invoiceReceivedAt == null ? null : ZonedDateTime.ofInstant(invoiceReceivedAt.toInstant(), ZoneOffset.UTC))
                .paidAt(paidAt == null ? null : ZonedDateTime.ofInstant(paidAt.toInstant(), ZoneOffset.UTC))
                .withAdditionalNetworks(Arrays.stream(networks).map(NetworkEnumEntity::toNetwork).collect(Collectors.toSet()))
                .amountUsdEquivalent(amountUsdEquivalent);
    }
}
