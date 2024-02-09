package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
public class RewardUsdEquivalentDataEntity {
    @Id
    UUID rewardId;
    Date rewardCreatedAt;
    UUID rewardCurrencyId;
    Date kycbVerifiedAt;
    Date currencyQuoteAvailableAt;
    Date unlockDate;
}
