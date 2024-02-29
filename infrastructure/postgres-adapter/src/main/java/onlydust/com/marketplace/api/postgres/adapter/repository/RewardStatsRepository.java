package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardStatsEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardStatsRepository extends JpaRepository<RewardStatsEntity, CurrencyEnumEntity> {
    @Query(value = """
            WITH reward_item_ids AS (SELECT ri.reward_id, JSONB_AGG(DISTINCT ri.id) as ids
                                     FROM reward_items ri
                                     GROUP BY ri.reward_id)
            SELECT r.currency_id                                                                                 AS currency_id,
                   COALESCE(SUM(r.amount), 0)                                                                    AS processed_amount,
                   COALESCE(SUM(rsd.amount_usd_equivalent), 0)                                                   AS processed_usd_amount,
                   COALESCE(SUM(r.amount) FILTER ( WHERE rs.status_for_user != 'COMPLETE' ), 0)                  AS pending_amount,
                   COALESCE(SUM(rsd.amount_usd_equivalent) FILTER ( WHERE rs.status_for_user != 'COMPLETE' ), 0) AS pending_usd_amount,
                   JSONB_AGG(r.id)                                                                               AS reward_ids,
                   COALESCE(JSONB_AGG(wii.ids) FILTER ( WHERE wii.ids IS NOT NULL ), '[]')                       AS reward_item_ids,
                   JSONB_AGG(r.project_id)                                                                       AS project_ids
            FROM rewards r
                     JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                     JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
                     JOIN iam.users u on u.github_user_id = r.recipient_id
                     LEFT JOIN reward_item_ids wii ON wii.reward_id = r.id
            WHERE u.id = :userId
              AND (COALESCE(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (COALESCE(:projectIds) IS NULL OR r.project_id IN (:projectIds))
              AND (:fromDate IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (:toDate IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            GROUP BY r.currency_id
            """, nativeQuery = true)
    List<RewardStatsEntity> findByUser(UUID userId, List<UUID> currencyIds, List<UUID> projectIds, String fromDate,
                                       String toDate);


    @Query(value = """
            SELECT DISTINCT pr.currency
            FROM payment_requests pr
            WHERE pr.recipient_id = :githubUserId
            ORDER BY pr.currency
            """, nativeQuery = true)
    List<CurrencyEnumEntity> listRewardCurrenciesByRecipient(Long githubUserId);
}
