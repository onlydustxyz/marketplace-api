package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardViewRepository extends JpaRepository<RewardViewEntity, UUID> {
    @Query(value = """
            SELECT r.id                         AS id,
                   r.requested_at               AS requested_at,
                   r.project_id                 AS project_id,
                   r.amount                     AS amount,
                   r.currency_id                AS currency_id,
                   rsd.amount_usd_equivalent    AS dollars_equivalent,
                   count(*)                     AS contribution_count,
                   github_recipient.id          AS recipient_id,
                   github_recipient.login       AS recipient_login,
                   user_avatar_url(r.recipient_id, github_recipient.avatar_url)  AS recipient_avatar_url,
                   github_requestor.id          AS requestor_id,
                   github_requestor.login       AS requestor_login,
                   user_avatar_url(github_requestor.id, github_requestor.avatar_url)  AS requestor_avatar_url,
                   receipt_id                   AS receipt_id
            from iam.users recipient
                 JOIN rewards r ON r.recipient_id = recipient.github_user_id
                 JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                 JOIN reward_items ri ON ri.reward_id = r.id
                 JOIN indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id
                 JOIN iam.users requestor ON requestor.id = r.requestor_id
                 JOIN indexer_exp.github_accounts github_requestor ON github_requestor.id = requestor.github_user_id
            WHERE recipient.id = :userId
              AND (coalesce(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (coalesce(:projectIds) IS NULL OR r.project_id IN (:projectIds))
              and (:fromDate IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (:toDate IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            GROUP BY 
                r.id, 
                r.requested_at, 
                r.project_id, 
                r.amount, 
                r.currency_id, 
                rsd.amount_usd_equivalent,
                github_recipient.id,
                github_recipient.login,
                github_recipient.avatar_url,
                github_requestor.id,
                github_requestor.login,
                github_requestor.avatar_url
            """, nativeQuery = true)
    Page<RewardViewEntity> findUserRewards(UUID userId, List<UUID> currencyIds, List<UUID> projectIds, String fromDate, String toDate, Pageable pageable);

    @Query(value = """
            SELECT r.id                         AS id,
                   r.requested_at               AS requested_at,
                   r.project_id                 AS project_id,
                   r.amount                     AS amount,
                   r.currency_id                AS currency_id,
                   rsd.amount_usd_equivalent    AS dollars_equivalent,
                   count(*)                     AS contribution_count,
                   github_recipient.id          AS recipient_id,
                   github_recipient.login       AS recipient_login,
                   user_avatar_url(r.recipient_id, github_recipient.avatar_url)  AS recipient_avatar_url,
                   github_requestor.id          AS requestor_id,
                   github_requestor.login       AS requestor_login,
                   user_avatar_url(github_requestor.id, github_requestor.avatar_url)  AS requestor_avatar_url,
                   receipt_id                   AS receipt_id
            from rewards r
                 JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                 JOIN reward_items ri ON ri.reward_id = r.id
                 JOIN indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id
                 JOIN iam.users requestor ON requestor.id = r.requestor_id
                 JOIN indexer_exp.github_accounts github_requestor ON github_requestor.id = requestor.github_user_id
            WHERE r.project_id = :projectId
              AND (coalesce(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (coalesce(:contributorIds) IS NULL OR r.recipient_id IN (:contributorIds))
              and (:fromDate IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (:toDate IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            GROUP BY 
                r.id, 
                r.requested_at, 
                r.project_id, 
                r.amount, 
                r.currency_id, 
                rsd.amount_usd_equivalent,
                github_recipient.id,
                github_recipient.login,
                github_recipient.avatar_url,
                github_requestor.id,
                github_requestor.login,
                github_requestor.avatar_url
            """, nativeQuery = true)
    Page<RewardViewEntity> findProjectRewards(UUID projectId, List<UUID> currencyIds, List<Long> contributorIds, String fromDate, String toDate,
                                              Pageable pageable);

    @Query(value = """
            SELECT r.id                         AS id,
                   r.requested_at               AS requested_at,
                   r.project_id                 AS project_id,
                   r.amount                     AS amount,
                   r.currency_id                AS currency_id,
                   rsd.amount_usd_equivalent    AS dollars_equivalent,
                   count(*)                     AS contribution_count,
                   github_recipient.id          AS recipient_id,
                   github_recipient.login       AS recipient_login,
                   user_avatar_url(r.recipient_id, github_recipient.avatar_url)  AS recipient_avatar_url,
                   github_requestor.id          AS requestor_id,
                   github_requestor.login       AS requestor_login,
                   user_avatar_url(github_requestor.id, github_requestor.avatar_url)  AS requestor_avatar_url,
                   receipt_id                   AS receipt_id
            from rewards r
                 JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                 JOIN reward_items ri ON ri.reward_id = r.id
                 JOIN indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id
                 JOIN iam.users requestor ON requestor.id = r.requestor_id
                 JOIN indexer_exp.github_accounts github_requestor ON github_requestor.id = requestor.github_user_id
            WHERE r.id = :rewardId
            GROUP BY 
                r.id, 
                r.requested_at, 
                r.project_id, 
                r.amount, 
                r.currency_id, 
                rsd.amount_usd_equivalent,
                github_recipient.id,
                github_recipient.login,
                github_recipient.avatar_url,
                github_requestor.id,
                github_requestor.login,
                github_requestor.avatar_url
            """, nativeQuery = true)
    Optional<RewardViewEntity> find(UUID rewardId);

    @Query(value = """
             SELECT r.id                         AS id,
                    r.requested_at               AS requested_at,
                    r.project_id                 AS project_id,
                    r.amount                     AS amount,
                    r.currency_id                AS currency_id,
                    rsd.amount_usd_equivalent    AS dollars_equivalent,
                    count(*)                     AS contribution_count,
                    github_recipient.id          AS recipient_id,
                    github_recipient.login       AS recipient_login,
                    user_avatar_url(r.recipient_id, github_recipient.avatar_url)  AS recipient_avatar_url,
                    github_requestor.id          AS requestor_id,
                    github_requestor.login       AS requestor_login,
                    user_avatar_url(github_requestor.id, github_requestor.avatar_url)  AS requestor_avatar_url,
                    receipt_id                   AS receipt_id
            from rewards r 
                  JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                  JOIN accounting.reward_statuses rs ON rs.reward_id = r.id AND rs.status_for_user = 'PENDING_REQUEST'
                  JOIN reward_items ri ON ri.reward_id = r.id
                 JOIN indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id
                 JOIN iam.users requestor ON requestor.id = r.requestor_id
                 JOIN indexer_exp.github_accounts github_requestor ON github_requestor.id = requestor.github_user_id
             WHERE r.recipient_id = :githubUserId
             GROUP BY 
                 r.id, 
                 r.requested_at, 
                 r.project_id, 
                 r.amount, 
                 r.currency_id, 
                 rsd.amount_usd_equivalent,
                github_recipient.id,
                github_recipient.login,
                github_recipient.avatar_url,
                github_requestor.id,
                github_requestor.login,
                github_requestor.avatar_url
             """, nativeQuery = true)
    List<RewardViewEntity> findPendingPaymentRequestForRecipient(Long githubUserId);

    static Sort sortBy(final UserRewardView.SortBy sortBy, final Sort.Direction direction) {
        return switch (sortBy) {
            case amount -> JpaSort.unsafe(direction, "coalesce(rsd.amount_usd_equivalent, 0)").and(Sort.by("requested_at").descending());
            case contribution -> Sort.by(direction, "contribution_count").and(Sort.by("requested_at").descending());
            case status -> Sort.by(direction, "status").and(Sort.by("requested_at").descending());
            case requestedAt -> Sort.by(direction, "requested_at");
        };
    }

    static Sort sortBy(final ProjectRewardView.SortBy sortBy, final Sort.Direction direction) {
        return switch (sortBy) {
            case amount -> JpaSort.unsafe(direction, "coalesce(rsd.amount_usd_equivalent, 0)").and(Sort.by("requested_at").descending());
            case contribution -> Sort.by(direction, "contribution_count").and(Sort.by("requested_at").descending());
            case status -> Sort.by(direction, "status").and(Sort.by("requested_at").descending());
            case requestedAt -> Sort.by(direction, "requested_at");
        };
    }
}
