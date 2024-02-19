package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InvoiceRewardRepository extends JpaRepository<InvoiceRewardEntity, UUID> {
    @Query(value = """
            SELECT
                pr.id                as id,
                pr.project_id        as project_id,
                pr.requested_at      as requested_at,
                pr.amount            as amount,
                c.id                 as currency_id,
                usd.id               as base_currency_id,
                hq.price * pr.amount as base_amount
            FROM
                payment_requests pr
                JOIN currencies c ON UPPER(CAST(pr.currency AS TEXT)) = c.code
                JOIN currencies usd ON usd.code = 'USD'
                JOIN LATERAL (
                    SELECT * FROM accounting.historical_quotes
                    WHERE currency_id = c.id AND
                    base_id = usd.id
                    ORDER BY timestamp DESC
                    LIMIT 1
                ) hq ON true
            WHERE
                pr.id IN :rewardIds
            """, nativeQuery = true)
    List<InvoiceRewardEntity> findAll(List<UUID> rewardIds);
}
