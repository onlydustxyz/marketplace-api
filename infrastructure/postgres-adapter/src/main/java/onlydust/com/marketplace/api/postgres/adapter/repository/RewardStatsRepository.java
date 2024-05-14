package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardStatsViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardStatsRepository extends JpaRepository<RewardStatsViewEntity, UUID> {
    @Query(value = """
            WITH reward_item_ids AS (SELECT ri.reward_id, JSONB_AGG(DISTINCT ri.id) as ids
                                     FROM reward_items ri
                                     GROUP BY ri.reward_id)
            SELECT r.currency_id                                                                        AS currency_id,
                   COALESCE(SUM(r.amount), 0)                                                           AS processed_amount,
                   COALESCE(SUM(rsd.amount_usd_equivalent), 0)                                          AS processed_usd_amount,
                   COALESCE(SUM(r.amount) FILTER ( WHERE rs.status != 'COMPLETE' ), 0)                  AS pending_amount,
                   COALESCE(COUNT(rs.status) FILTER (
                            WHERE rs.status = 'PENDING_REQUEST' AND rpu.role = 'ADMIN'
                        ), 0)                                                                           AS pending_request_count,
                   COALESCE(SUM(rsd.amount_usd_equivalent) FILTER ( WHERE rs.status != 'COMPLETE' ), 0) AS pending_usd_amount,
                   JSONB_AGG(r.id)                                                                      AS reward_ids,
                   COALESCE(JSONB_AGG(wii.ids) FILTER ( WHERE wii.ids IS NOT NULL ), '[]')              AS reward_item_ids,
                   JSONB_AGG(r.project_id)                                                              AS project_ids
            FROM rewards r
                     JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                     JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
                     LEFT JOIN reward_item_ids wii ON wii.reward_id = r.id
                     LEFT JOIN iam.users caller ON caller.github_user_id = :contributorId
                     LEFT JOIN accounting.billing_profiles_users rpu ON rpu.billing_profile_id = r.billing_profile_id AND rpu.user_id = caller.id
            WHERE (
                    (coalesce(:administratedBillingProfileIds) IS NULL AND r.recipient_id = :contributorId)
                    OR
                    (coalesce(:administratedBillingProfileIds) IS NOT NULL AND
                        (
                            (r.billing_profile_id IN (:administratedBillingProfileIds) AND r.recipient_id != :contributorId)
                            OR
                            r.recipient_id = :contributorId
                        )
                    )
                  )
              AND (COALESCE(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (COALESCE(:projectIds) IS NULL OR r.project_id IN (:projectIds))
              AND (:fromDate IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (:toDate IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            GROUP BY r.currency_id
            """, nativeQuery = true)
    List<RewardStatsViewEntity> findByUser(Long contributorId, List<UUID> currencyIds, List<UUID> projectIds, List<UUID> administratedBillingProfileIds,
                                           String fromDate, String toDate);
}
