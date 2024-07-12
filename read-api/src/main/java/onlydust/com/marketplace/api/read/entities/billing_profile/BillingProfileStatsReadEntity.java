package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Accessors(fluent = true, chain = true)
@Table(name = "billing_profile_stats", schema = "accounting")
@Immutable
public class BillingProfileStatsReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID billingProfileId;

    @OneToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    @NonNull
    BillingProfileReadEntity billingProfile;

    @NonNull
    Integer rewardCount;
    @NonNull
    Integer invoiceableRewardCount;
    @NonNull
    Boolean missingPayoutInfo;
    @NonNull
    Boolean missingVerification;
    @NonNull
    Boolean individualLimitReached;
    @NonNull
    BigDecimal currentYearPaymentAmount;
    @NonNull
    BigDecimal currentYearPaymentLimit;
    @NonNull
    Boolean mandateAcceptanceOutdated;
}
