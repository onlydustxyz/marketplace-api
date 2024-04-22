package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InvoiceRewardRepository extends JpaRepository<InvoiceRewardEntity, UUID> {
    @Query(value = """
            SELECT
                r.id                          as id,
                p.name                        as project_name,
                r.requested_at                as requested_at,
                r.amount                      as amount,
                r.currency_id                 as currency_id,
                usd.id                        as target_currency_id,
                rsd.amount_usd_equivalent     as target_amount,
                r.invoice_id                  as invoice_id,
                CAST(array[] AS accounting.network[]) as networks
            FROM
                rewards r
                JOIN accounting.reward_status_data rsd on rsd.reward_id = r.id
                JOIN currencies usd ON usd.code = 'USD'
                JOIN projects p ON r.project_id = p.id
            WHERE
                r.id IN :rewardIds
            """, nativeQuery = true)
    List<InvoiceRewardEntity> findAll(List<UUID> rewardIds);
}
