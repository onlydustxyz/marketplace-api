package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    Boolean isIndividual;
    @NonNull Boolean kycbVerified;
    Boolean usRecipient;

    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    @NonNull CurrencyEnumEntity rewardCurrency;

    @NonNull BigDecimal currentYearUsdTotal;
    @NonNull Boolean payoutInfoFilled;
    @NonNull Boolean sponsorHasEnoughFund;
    Date unlockDate;
    @NonNull Boolean paymentRequested;
    @NonNull Boolean paid;

    public static RewardStatusEntity of(RewardStatus rewardStatus) {
        return RewardStatusEntity.builder()
                .rewardId(rewardStatus.rewardId().value())
                .isIndividual(rewardStatus.isIndividual())
                .kycbVerified(rewardStatus.kycbVerified())
                .usRecipient(rewardStatus.usRecipient())
                .rewardCurrency(CurrencyEnumEntity.of(rewardStatus.rewardCurrency()))
                .currentYearUsdTotal(rewardStatus.currentYearUsdTotal().getValue())
                .payoutInfoFilled(rewardStatus.payoutInfoFilled())
                .sponsorHasEnoughFund(rewardStatus.sponsorHasEnoughFund())
                .unlockDate(Date.from(rewardStatus.unlockDate().toInstant()))
                .paymentRequested(rewardStatus.paymentRequested())
                .paid(rewardStatus.paid())
                .build();
    }

    public RewardStatus toRewardStatus(CurrencyRepository currencyRepository) {
        return new RewardStatus(RewardId.of(rewardId))
                .isIndividual(isIndividual)
                .kycbVerified(kycbVerified)
                .usRecipient(usRecipient)
                .rewardCurrency(currencyRepository.findByCode(rewardCurrency.toString().toUpperCase()).map(CurrencyEntity::toDomain)
                        .orElseThrow(() -> OnlyDustException.internalServerError("Currency %s not found".formatted(rewardCurrency.toString()))))
                .currentYearUsdTotal(PositiveAmount.of(currentYearUsdTotal))
                .payoutInfoFilled(payoutInfoFilled)
                .sponsorHasEnoughFund(sponsorHasEnoughFund)
                .unlockDate(unlockDate != null ? ZonedDateTime.ofInstant(unlockDate.toInstant(), ZoneOffset.UTC) : null)
                .paymentRequested(paymentRequested)
                .paid(paid);
    }
}
