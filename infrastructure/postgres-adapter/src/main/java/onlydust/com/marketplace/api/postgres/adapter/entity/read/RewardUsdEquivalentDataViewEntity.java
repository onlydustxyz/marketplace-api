package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardUsdEquivalent;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@Table(name = "reward_usd_equivalent_data", schema = "accounting")
@Immutable
public class RewardUsdEquivalentDataViewEntity {
    @Id
    @NonNull
    UUID rewardId;
    @NonNull
    Date rewardCreatedAt;
    @NonNull
    UUID rewardCurrencyId;
    Date kycbVerifiedAt;
    Date currencyQuoteAvailableAt;
    Date unlockDate;
    @NonNull
    BigDecimal rewardAmount;

    public RewardUsdEquivalent toDomain() {
        return new RewardUsdEquivalent(
                RewardId.of(rewardId),
                ZonedDateTime.ofInstant(rewardCreatedAt.toInstant(), UTC),
                Currency.Id.of(rewardCurrencyId),
                Optional.ofNullable(kycbVerifiedAt)
                        .map(date -> ZonedDateTime.ofInstant(date.toInstant(), UTC))
                        .orElse(null),
                Optional.ofNullable(currencyQuoteAvailableAt)
                        .map(date -> ZonedDateTime.ofInstant(date.toInstant(), UTC))
                        .orElse(null),
                Optional.ofNullable(unlockDate)
                        .map(date -> ZonedDateTime.ofInstant(date.toInstant(), UTC))
                        .orElse(null),
                rewardAmount
        );
    }
}
