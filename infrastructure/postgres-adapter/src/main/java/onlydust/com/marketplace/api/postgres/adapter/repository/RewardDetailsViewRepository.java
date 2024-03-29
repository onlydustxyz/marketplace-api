package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardDetailsViewEntity;
import onlydust.com.marketplace.project.domain.model.Reward;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardDetailsViewRepository extends JpaRepository<RewardDetailsViewEntity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
            SELECT r.id                         AS id,
                   r.requested_at               AS requested_at,
                   r.project_id                 AS project_id,
                   r.amount                     AS amount,
                   r.currency_id                AS currency_id,
                   r.invoice_id                 AS invoice_id,
                   r.billing_profile_id         AS billing_profile_id,
                   ri.count                     AS contribution_count,
                   github_recipient.id          AS recipient_id,
                   github_recipient.login       AS recipient_login,
                   user_avatar_url(r.recipient_id, github_recipient.avatar_url)  AS recipient_avatar_url,
                   github_recipient.html_url    AS recipient_html_url,
                   recipient.id IS NOT NULL     AS recipient_is_registered,
                   github_requestor.id          AS requestor_id,
                   github_requestor.login       AS requestor_login,
                   user_avatar_url(github_requestor.id, github_requestor.avatar_url)  AS requestor_avatar_url,
                   github_requestor.html_url    AS requestor_html_url
            from rewards r
                 JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                 JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
                 JOIN LATERAL (SELECT count(*) as count from reward_items ri where reward_id = r.id) ri ON TRUE
                 JOIN indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id
                 LEFT JOIN iam.users recipient ON recipient.github_user_id = r.recipient_id
                 JOIN iam.users requestor ON requestor.id = r.requestor_id
                 JOIN indexer_exp.github_accounts github_requestor ON github_requestor.id = requestor.github_user_id
            """;

    static Sort sortBy(final Reward.SortBy sortBy, final Sort.Direction direction) {
        return switch (sortBy) {
            case AMOUNT -> JpaSort.unsafe(direction, "coalesce(rsd.amount_usd_equivalent, 0)").and(Sort.by("requested_at").descending());
            case CONTRIBUTION -> Sort.by(direction, "contribution_count").and(Sort.by("requested_at").descending());
            case STATUS -> Sort.by(direction, "rs.status").and(Sort.by("requested_at").descending());
            case REQUESTED_AT -> Sort.by(direction, "requested_at");
        };
    }

    @Query(value = SELECT + """
            WHERE r.project_id = :projectId
              AND (coalesce(:contributorIds) IS NULL OR r.recipient_id IN (:contributorIds))
              AND (coalesce(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (:fromDate IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (:toDate IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            """, nativeQuery = true)
    Page<RewardDetailsViewEntity> findProjectRewards(UUID projectId, List<UUID> currencyIds, List<Long> contributorIds, String fromDate, String toDate,
                                                     Pageable pageable);

    @Query(value = SELECT + """
            WHERE (
                r.recipient_id = :githubUserId
                OR
                (coalesce(:administratedBillingProfileIds) IS NOT NULL AND r.billing_profile_id IN (:administratedBillingProfileIds))
              )
              AND (coalesce(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (coalesce(:projectIds) IS NULL OR r.project_id IN (:projectIds))
              AND (:fromDate IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (:toDate IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            """, nativeQuery = true)
    Page<RewardDetailsViewEntity> findUserRewards(Long githubUserId, List<UUID> currencyIds, List<UUID> projectIds, List<UUID> administratedBillingProfileIds,
                                                  String fromDate, String toDate, Pageable pageable);

    @Modifying
    @Query(nativeQuery = true, value = """
            update rewards
            set payment_notified_at = now()
            where id in (:rewardIds)
            """)
    void markRewardAsPaymentNotified(List<UUID> rewardIds);
}
