package onlydust.com.marketplace.api.postgres.adapter.repository;

import io.micrometer.common.lang.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface RewardStatusRepository extends JpaRepository<RewardStatusDataEntity, UUID> {
    @Query(value = """
            SELECT rsd FROM RewardStatusDataEntity rsd
            WHERE rsd.status.status <= 'PENDING_REQUEST'
            """)
    List<RewardStatusDataEntity> findNotRequested();

    @Query(value = """
            SELECT rsd.* FROM accounting.reward_status_data rsd
            JOIN accounting.reward_statuses r on r.reward_id = rsd.reward_id AND r.status <= 'PENDING_REQUEST'
            JOIN iam.users u on r.recipient_id = u.github_user_id
            JOIN accounting.payout_preferences pp ON pp.project_id = r.project_id AND pp.user_id = u.id
            JOIN accounting.billing_profiles bp ON bp.id = pp.billing_profile_id AND bp.id = :billingProfileId
            """, nativeQuery = true)
    List<RewardStatusDataEntity> findNotRequestedByBillingProfile(UUID billingProfileId);


    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RewardStatusDataEntity r set r.invoiceReceivedAt = :invoiceReceivedAt where r.rewardId = :rewardId")
    int updateInvoiceReceivedAt(@NonNull UUID rewardId,
                                Date invoiceReceivedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RewardStatusDataEntity r set r.paidAt = :paidAt where r.rewardId = :rewardId")
    int updatePaidAt(@NonNull UUID rewardId,
                     Date paidAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RewardStatusDataEntity r set
            r.amountUsdEquivalent = :amountUsdEquivalent,
            r.usdConversionRate = :usdConversionRate
            where r.rewardId = :rewardId
            """)
    int updateUsdAmount(@NonNull UUID rewardId,
                        BigDecimal amountUsdEquivalent,
                        BigDecimal usdConversionRate);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RewardStatusDataEntity r set
            r.sponsorHasEnoughFund = :sponsorHasEnoughFund,
            r.unlockDate = :unlockDate,
            r.networks = :networks,
            r.amountUsdEquivalent = :amountUsdEquivalent,
            r.usdConversionRate = :usdConversionRate
            where r.rewardId = :rewardId
            """)
    int updateAccountingData(@NonNull UUID rewardId,
                             @NonNull Boolean sponsorHasEnoughFund,
                             Date unlockDate,
                             @NonNull NetworkEnumEntity[] networks,
                             BigDecimal amountUsdEquivalent,
                             BigDecimal usdConversionRate);
}
