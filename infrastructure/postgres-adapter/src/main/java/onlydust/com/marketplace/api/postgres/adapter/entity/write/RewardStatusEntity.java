package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class RewardStatusEntity {
    @Id
    @NonNull UUID rewardId;

    Boolean sponsorHasEnoughFund;
    Date unlockDate;
    Date paymentRequestedAt;
    Date paidAt;

    public static RewardStatusEntity of(RewardStatus rewardStatus) {
        return RewardStatusEntity.builder()
                .rewardId(rewardStatus.rewardId().value())
                .sponsorHasEnoughFund(rewardStatus.sponsorHasEnoughFund())
                .unlockDate(rewardStatus.unlockDate().map(ChronoZonedDateTime::toInstant).map(Date::from).orElse(null))
                .paymentRequestedAt(rewardStatus.paymentRequestedAt().map(ChronoZonedDateTime::toInstant).map(Date::from).orElse(null))
                .paidAt(rewardStatus.paidAt().map(ChronoZonedDateTime::toInstant).map(Date::from).orElse(null))
                .build();
    }

    public RewardStatus toRewardStatus() {
        return new RewardStatus(RewardId.of(rewardId))
                .sponsorHasEnoughFund(Boolean.TRUE.equals(sponsorHasEnoughFund))
                .unlockDate(unlockDate == null ? null : ZonedDateTime.ofInstant(unlockDate.toInstant(), ZoneOffset.UTC))
                .paymentRequestedAt(paymentRequestedAt == null ? null : ZonedDateTime.ofInstant(paymentRequestedAt.toInstant(), ZoneOffset.UTC))
                .paidAt(paidAt == null ? null : ZonedDateTime.ofInstant(paidAt.toInstant(), ZoneOffset.UTC));
    }
}
