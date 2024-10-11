package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.UserRewardStatsReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRewardStatsReadRepository extends JpaRepository<UserRewardStatsReadEntity, UUID> {
    @Query(value = """
            WITH reward_item_ids AS (SELECT ri.reward_id, JSONB_AGG(DISTINCT ri.id) as ids
                                     FROM reward_items ri
                                     GROUP BY ri.reward_id)
            SELECT r.currency_id                                                                       AS currency_id,
                   COALESCE(SUM(r.amount), 0)                                                          AS processed_amount,
                   COALESCE(SUM(r.amount_usd_equivalent), 0)                                           AS processed_usd_amount,
                   COALESCE(SUM(r.amount) FILTER ( WHERE r.status != 'COMPLETE' ), 0)                  AS pending_amount,
                   COALESCE(COUNT(r.status) FILTER (
                            WHERE r.status = 'PENDING_REQUEST' AND rpu.role = 'ADMIN'
                        ), 0)                                                                           AS pending_request_count,
                   COALESCE(SUM(r.amount_usd_equivalent) FILTER ( WHERE r.status != 'COMPLETE' ), 0)    AS pending_usd_amount,
                   JSONB_AGG(r.reward_id)                                                               AS reward_ids,
                   COALESCE(JSONB_AGG(wii.ids) FILTER ( WHERE wii.ids IS NOT NULL ), '[]')              AS reward_item_ids,
                   JSONB_AGG(r.project_id)                                                              AS project_ids
            FROM accounting.reward_statuses r
                     LEFT JOIN reward_item_ids wii ON wii.reward_id = r.reward_id
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
              AND (COALESCE(:fromDate) IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (COALESCE(:toDate) IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            GROUP BY r.currency_id
            """, nativeQuery = true)
    List<UserRewardStatsReadEntity> findByUser(Long contributorId, List<UUID> currencyIds, List<UUID> projectIds, List<UUID> administratedBillingProfileIds,
                                               String fromDate, String toDate);


}
