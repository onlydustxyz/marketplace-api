package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRewardViewRepository extends JpaRepository<UserRewardViewEntity, UUID> {
    @Query(value = """
            SELECT r.id                         AS id,
                   r.requested_at               AS requested_at,
                   r.project_id                 AS project_id,
                   r.amount                     AS amount,
                   r.currency_id                AS currency_id,
                   rsd.amount_usd_equivalent    AS dollars_equivalent,
                   count(*)                     AS contribution_count
            from iam.users u
                 JOIN rewards r ON r.recipient_id = u.github_user_id
                 JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                 JOIN reward_items ri ON ri.reward_id = r.id
            WHERE u.id = :userId
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
                rsd.amount_usd_equivalent
            """, nativeQuery = true)
    Page<UserRewardViewEntity> find(UUID userId, List<UUID> currencyIds, List<UUID> projectIds, String fromDate, String toDate, Pageable pageable);

    @Query(value = """
            SELECT r.id                         AS id,
                   r.requested_at               AS requested_at,
                   r.project_id                 AS project_id,
                   r.amount                     AS amount,
                   r.currency_id                AS currency_id,
                   rsd.amount_usd_equivalent    AS dollars_equivalent,
                   count(*)                     AS contribution_count
            from rewards r 
                 JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                 JOIN accounting.reward_statuses rs ON rs.reward_id = r.id AND rs.status = 'PENDING_REQUEST'
                 JOIN reward_items ri ON ri.reward_id = r.id
            WHERE r.recipient_id = :githubUserId
            GROUP BY 
                r.id, 
                r.requested_at, 
                r.project_id, 
                r.amount, 
                r.currency_id, 
                rsd.amount_usd_equivalent
            """, nativeQuery = true)
    List<UserRewardViewEntity> findPendingPaymentRequestForRecipient(Long githubUserId);

    static Sort sortBy(final UserRewardView.SortBy sortBy, final Sort.Direction direction) {
        return switch (sortBy) {
            case amount -> Sort.by(Sort.Order.by("dollars_equivalent").with(direction).nullsLast()).and(Sort.by("requested_at").descending());
            case contribution -> Sort.by(direction, "contribution_count").and(Sort.by("requested_at").descending());
            case status -> Sort.by(direction, "status").and(Sort.by("requested_at").descending());
            case requestedAt -> Sort.by(direction, "requested_at");
        };
    }
}
