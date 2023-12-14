package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BudgetStatsRepository extends JpaRepository<BudgetStatsEntity, Long> {
    @Query(value = """
            SELECT
                b.currency,
                b.remaining_amount AS remaining_amount,
                b.remaining_amount * CASE WHEN b.currency = 'usd' THEN 1 ELSE cuq.price END AS remaining_usd_amount,
                COALESCE(SUM(pr.amount), 0) AS spent_amount,
                COALESCE(SUM(pr.amount), 0) * CASE WHEN b.currency = 'usd' THEN 1 ELSE cuq.price END AS spent_usd_amount,
                COALESCE(JSONB_AGG(DISTINCT pr.id) FILTER ( WHERE pr.id IS NOT NULL ), '[]') AS reward_ids,
                COALESCE(JSONB_AGG(DISTINCT wi.id) FILTER ( WHERE wi.id IS NOT NULL ), '[]') AS reward_item_ids,
                COALESCE(JSONB_AGG(DISTINCT pr.recipient_id) FILTER ( WHERE pr.recipient_id IS NOT NULL ), '[]') AS reward_recipient_ids
            FROM budgets b
                JOIN projects_budgets pb ON pb.budget_id = b.id
                LEFT JOIN payment_requests pr ON pr.project_id = pb.project_id AND pr.currency = b.currency
                LEFT JOIN work_items wi ON wi.payment_id = pr.id
                LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = b.currency
            WHERE
                pb.project_id = :projectId AND 
                (COALESCE(:currencies) IS NULL OR CAST(pr.currency AS TEXT) IN (:currencies)) AND
                (COALESCE(:contributorIds) IS NULL OR pr.recipient_id IN (:contributorIds)) AND
                (:fromDate IS NULL OR pr.requested_at >= TO_DATE(CAST(:fromDate AS TEXT), 'YYYY-MM-DD')) AND
                (:toDate IS NULL OR pr.requested_at < TO_DATE(CAST(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            GROUP BY
                b.currency, 
                b.initial_amount, 
                b.remaining_amount, 
                cuq.price
            """, nativeQuery = true)
    List<BudgetStatsEntity> findByProject(UUID projectId, List<String> currencies, List<Long> contributorIds,
                                    String fromDate, String toDate);
}
