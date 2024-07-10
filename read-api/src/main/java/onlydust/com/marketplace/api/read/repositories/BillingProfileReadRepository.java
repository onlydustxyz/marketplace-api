package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.billing_profile.BillingProfileReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BillingProfileReadRepository extends Repository<BillingProfileReadEntity, UUID> {

    @Query(value = """
            select distinct bp
            from BillingProfileReadEntity bp
                join fetch bp.stats
                join bp.users u on u.userId = :userId
            """)
    List<BillingProfileReadEntity> findByUserId(@Param("userId") UUID userId);

}
