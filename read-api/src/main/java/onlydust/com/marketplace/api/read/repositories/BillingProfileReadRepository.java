package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.billing_profile.BillingProfileReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingProfileReadRepository extends Repository<BillingProfileReadEntity, UUID> {

    @Query(value = """
            select distinct bp
            from BillingProfileReadEntity bp
                join fetch bp.stats
                join bp.users u on u.userId = :userId
            """)
    List<BillingProfileReadEntity> findByUserId(@Param("userId") UUID userId);

    @Query(value = """
            select distinct bp
            from BillingProfileReadEntity bp
            left join fetch bp.kyc
            left join fetch bp.kyb
            left join fetch bp.currentMonthRewards r
            join fetch bp.users u
            left join fetch bp.payoutInfo
            left join fetch r.currency c
            left join fetch c.latestUsdQuote
            join fetch u.user
            where bp.id = :billingProfileId
            """)
    Optional<BillingProfileReadEntity> findById(UUID billingProfileId);

    @Query(value = """
            select distinct bp
            from BillingProfileReadEntity bp
            left join fetch bp.missingPayoutInfoRewards r
            join fetch bp.users u
            left join fetch bp.payoutInfo
            where bp.id = :billingProfileId
            """)
    Optional<BillingProfileReadEntity> findPayoutInfosById(UUID billingProfileId);
}
