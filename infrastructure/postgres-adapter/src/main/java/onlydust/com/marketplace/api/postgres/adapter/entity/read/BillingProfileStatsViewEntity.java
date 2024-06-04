package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Value
@Accessors(fluent = true)
@NoArgsConstructor(force = true)
@Table(name = "billing_profile_stats", schema = "accounting")
@Immutable
public class BillingProfileStatsViewEntity {
    @Id
    UUID billingProfileId;
    Integer rewardCount;
    Integer invoiceableRewardCount;
    Boolean missingPayoutInfo;
    Boolean missingVerification;
    Boolean individualLimitReached;
    BigDecimal currentYearPaymentAmount;
    Boolean mandateAcceptanceOutdated;
}
