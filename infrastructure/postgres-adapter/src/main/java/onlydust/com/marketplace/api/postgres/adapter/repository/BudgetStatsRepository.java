package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BudgetStatsRepository extends JpaRepository<BudgetStatsEntity, Long> {
    @Query(value = """
            WITH work_item_ids AS (SELECT ri.reward_id, JSONB_AGG(DISTINCT ri.id) as ids FROM reward_items ri GROUP BY ri.reward_id)
            SELECT
                b.currency,
                b.remaining_amount AS remaining_amount,
                b.remaining_amount * CASE WHEN b.currency = 'usd' THEN 1 ELSE cuq.price END AS remaining_usd_amount,
                COALESCE(SUM(r.amount), 0) AS spent_amount,
                COALESCE(SUM(r.amount), 0) * CASE WHEN b.currency = 'usd' THEN 1 ELSE cuq.price END AS spent_usd_amount,
                COALESCE(JSONB_AGG(DISTINCT r.id) FILTER ( WHERE r.id IS NOT NULL ), '[]') AS reward_ids,
                COALESCE(JSONB_AGG(wii.ids) FILTER ( WHERE wii.ids IS NOT NULL ), '[]') AS reward_item_ids,
                COALESCE(JSONB_AGG(DISTINCT r.recipient_id) FILTER ( WHERE r.recipient_id IS NOT NULL ), '[]') AS reward_recipient_ids
            FROM budgets b
                JOIN projects_budgets pb ON pb.budget_id = b.id
                JOIN currencies c on c.code = UPPER(CAST(b.currency AS TEXT))
                LEFT JOIN rewards r ON 
                    r.project_id = pb.project_id AND 
                    r.currency_id = c.id AND 
                    (COALESCE(:contributorIds) IS NULL OR r.recipient_id IN (:contributorIds)) AND
                    (:fromDate IS NULL OR r.requested_at >= TO_DATE(CAST(:fromDate AS TEXT), 'YYYY-MM-DD')) AND
                    (:toDate IS NULL OR r.requested_at < TO_DATE(CAST(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
                LEFT JOIN work_item_ids wii ON wii.reward_id = r.id
                LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = b.currency
            WHERE
                pb.project_id = :projectId AND 
                (COALESCE(:currencies) IS NULL OR c.id IN (:currencies))
                
            GROUP BY
                b.currency, 
                b.initial_amount, 
                b.remaining_amount, 
                cuq.price
            """, nativeQuery = true)
    List<BudgetStatsEntity> findByProject(UUID projectId, List<UUID> currencies, List<Long> contributorIds,
                                          String fromDate, String toDate);
}
