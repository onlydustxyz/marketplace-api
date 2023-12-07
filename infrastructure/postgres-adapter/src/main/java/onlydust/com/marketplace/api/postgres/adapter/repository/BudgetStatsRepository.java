package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BudgetStatsRepository extends JpaRepository<BudgetStatsEntity, Long> {
    @Query(value = """
            WITH reward_stats AS (
                SELECT
                    pr.project_id,
                    pr.currency,
                    sum(pr.amount)                  AS spent_amount,
                    sum(pr.amount) * CASE WHEN pr.currency = 'usd' THEN 1 ELSE cuq.price END AS spent_usd_amount,
                    COUNT(DISTINCT pr.recipient_id) AS reward_recipients_count,
                    COUNT(DISTINCT pr.id)           AS rewards_count,
                    COUNT(DISTINCT wi.id)           AS reward_items_count
               FROM payment_requests pr
               JOIN public.work_items wi ON pr.id = wi.payment_id
               LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = pr.currency
               WHERE 
                    COALESCE(:contributorIds) IS NULL OR pr.recipient_id IN (:contributorIds) AND
                    (:fromDate IS NULL OR pr.requested_at >= to_date(cast(:fromDate as text), 'YYYY-MM-DD')) AND
                    (:toDate IS NULL OR pr.requested_at < to_date(cast(:toDate as text), 'YYYY-MM-DD') + 1)
               GROUP BY pr.project_id, pr.currency, cuq.price
            )
            SELECT
                1 AS ID,
                CASE WHEN :currency IS NOT NULL THEN COALESCE(SUM(b.remaining_amount), 0) END AS remaining_amount,
                SUM(COALESCE(b.remaining_amount, 0) * CASE WHEN b.currency = 'usd' THEN 1 ELSE cuq.price END) AS remaining_usd_amount,
                CASE WHEN :currency IS NOT NULL THEN COALESCE(SUM(rs.spent_amount), 0) END AS spent_amount,
                SUM(rs.spent_usd_amount) AS spent_usd_amount,
                COALESCE(SUM(rs.rewards_count), 0) AS rewards_count,
                COALESCE(SUM(rs.reward_items_count), 0) AS reward_items_count,
                COALESCE(SUM(rs.reward_recipients_count), 0) AS reward_recipients_count
            FROM
                 budgets b
            JOIN projects_budgets pb ON pb.budget_id = b.id
            LEFT JOIN reward_stats rs ON rs.project_id = pb.project_id AND rs.currency = b.currency
            LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = b.currency
            WHERE 
                pb.project_id = :projectId AND 
                (:currency IS NULL OR b.currency = CAST(CAST(:currency AS TEXT) AS currency))
            """, nativeQuery = true)
    BudgetStatsEntity findByProject(UUID projectId, String currency, List<Long> contributorIds,
                                    String fromDate, String toDate);
}
