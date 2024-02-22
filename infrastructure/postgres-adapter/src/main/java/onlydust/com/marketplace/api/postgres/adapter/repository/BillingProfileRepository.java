package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BillingProfileRepository extends JpaRepository<BillingProfileEntity, UUID>{

    @Query(value = """
            select bp.* from accounting.billing_profiles bp
            join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
            where bp.type = 'INDIVIDUAL'
            """, nativeQuery = true)
    List<BillingProfileEntity> findIndividualProfilesForUserId(@Param("userId") UUID userId);

    @Query(value = """
            select bp.* from accounting.billing_profiles bp
            join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
            """, nativeQuery = true)
    List<BillingProfileEntity> findBillingProfilesForUserId(@Param("userId") UUID userId);

}
