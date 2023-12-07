package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetStatsEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BudgetStatsRepository extends JpaRepository<BudgetStatsEntity, CurrencyEnumEntity> {
    @Query(value = """
            WITH reward_stats AS (
                SELECT
                    pr.project_id,
                    pr.currency,
                    count(DISTINCT pr.recipient_id) AS reward_recipients_count,
                    count(DISTINCT pr.id)           AS rewards_count,
                    count(DISTINCT wi.id)           AS reward_items_count
               FROM payment_requests pr
               JOIN public.work_items wi ON pr.id = wi.payment_id
               GROUP BY pr.project_id, pr.currency
            )
            SELECT
                b.currency,
                b.remaining_amount,
                b.remaining_amount * CASE WHEN b.currency = 'usd' THEN 1 ELSE cuq.price END AS remaining_usd_amount,
                b.initial_amount - b.remaining_amount AS spent_amount,
                (b.initial_amount - b.remaining_amount) * CASE WHEN b.currency = 'usd' THEN 1 ELSE cuq.price END AS spent_usd_amount,
                rs.rewards_count,
                rs.reward_items_count,
                rs.reward_recipients_count
            FROM
                 budgets b
            JOIN projects_budgets pb ON pb.budget_id = b.id
            JOIN reward_stats rs ON rs.project_id = pb.project_id AND rs.currency = b.currency
            LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = b.currency
            WHERE pb.project_id = :projectId AND b.currency = CAST(:currency AS currency)
            """, nativeQuery = true)
    BudgetStatsEntity findByProject(UUID projectId, String currency);
}
