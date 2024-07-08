package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.backoffice.api.contract.model.BatchPaymentStatus;
import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
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

    @Query("""
            SELECT bp
            FROM BatchPaymentReadEntity bp
            JOIN FETCH bp.rewards r
            JOIN FETCH r.project
            JOIN FETCH r.currency c
            JOIN FETCH r.statusData
            JOIN FETCH r.status
            JOIN FETCH c.latestUsdQuote
            WHERE COALESCE(:statuses, NULL) IS NULL OR bp.status IN :statuses
            """)
    Page<BatchPaymentReadEntity> findAllByStatusIn(List<BatchPaymentStatus> statuses, Pageable pageable);
}
