package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AllBillingProfileUserReadRepository extends JpaRepository<AllBillingProfileUserReadEntity, AllBillingProfileUserReadEntity.PrimaryKey> {

    @Query("""
            select bpu
            from AllBillingProfileUserReadEntity bpu
                join fetch bpu.billingProfile
            where bpu.userId=:userId
            """)
    List<AllBillingProfileUserReadEntity> findAllByUserId(UUID userId);
}
