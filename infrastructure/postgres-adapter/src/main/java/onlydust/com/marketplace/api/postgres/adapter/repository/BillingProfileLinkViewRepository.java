package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileLinkViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BillingProfileLinkViewRepository extends JpaRepository<BillingProfileLinkViewEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select bp.id,
                   bp.type,
                   bp.verification_status,
                   bpu.role
            from accounting.billing_profiles bp
            join accounting.billing_profiles_users bpu on bpu.billing_profile_id = bp.id and bpu.user_id = :userId
                """)
    List<BillingProfileLinkViewEntity> findByUserId(final UUID userId);
}
