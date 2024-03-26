package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Value
@Accessors(fluent = true)
@NoArgsConstructor(force = true)
@Table(name = "billing_profile_stats", schema = "accounting")
public class BillingProfileStatsViewEntity {
    @Id
    UUID billingProfileId;
    Integer rewardCount;
    Integer invoiceableRewardCount;
    Boolean missingPayoutInfo;
    Boolean missingVerification;
    BigDecimal currentYearPaymentAmount;
}
