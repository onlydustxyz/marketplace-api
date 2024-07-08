package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface BatchPaymentReadRepository extends Repository<BatchPaymentReadEntity, UUID> {

    @Query("""
            SELECT bp
            FROM BatchPaymentReadEntity bp
            JOIN FETCH bp.rewards r
            JOIN FETCH r.project
            JOIN FETCH r.currency c
            JOIN FETCH r.statusData
            JOIN FETCH r.status
            JOIN FETCH c.latestUsdQuote
            WHERE bp.id = :id
            """)
    Optional<BatchPaymentReadEntity> findById(UUID id);
}
