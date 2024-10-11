package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.contract.model.RewardsSort;
import onlydust.com.marketplace.api.contract.model.SortDirection;
import onlydust.com.marketplace.api.read.entities.reward.RewardDetailsReadEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

public interface RewardDetailsReadRepository extends JpaRepository<RewardDetailsReadEntity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
            SELECT r.reward_id                  AS id,
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
            from accounting.reward_statuses r
                 JOIN LATERAL (SELECT count(*) as count from reward_items ri where reward_id = r.reward_id) ri ON TRUE
                 JOIN indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id
                 LEFT JOIN iam.users recipient ON recipient.github_user_id = r.recipient_id
                 JOIN iam.users requestor ON requestor.id = r.requestor_id
                 JOIN indexer_exp.github_accounts github_requestor ON github_requestor.id = requestor.github_user_id
            """;

    static Sort sortBy(final RewardsSort sort, final SortDirection sortDirection) {
        final Sort.Direction jpaSortDirection = isNull(sortDirection) ? Sort.Direction.ASC : switch (sortDirection) {
            case ASC -> Sort.Direction.ASC;
            case DESC -> Sort.Direction.DESC;
        };
        final Sort defaultSort = Sort.by(jpaSortDirection, "requested_at");
        return isNull(sort) ? defaultSort : switch (sort) {
            case AMOUNT -> JpaSort.unsafe(jpaSortDirection, "coalesce(r.amount_usd_equivalent, 0)").and(Sort.by("requested_at").descending());
            case CONTRIBUTION -> Sort.by(jpaSortDirection, "contribution_count").and(Sort.by("requested_at").descending());
            case STATUS -> Sort.by(jpaSortDirection, "r.status").and(Sort.by("requested_at").descending());
            case REQUESTED_AT -> defaultSort;
        };
    }

    @Query(value = SELECT + """
            WHERE r.project_id = :projectId
              AND (coalesce(:contributorIds) IS NULL OR r.recipient_id IN (:contributorIds))
              AND (coalesce(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (coalesce(:fromDate) IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (coalesce(:toDate) IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
            """, nativeQuery = true)
    Page<RewardDetailsReadEntity> findProjectRewards(UUID projectId, List<UUID> currencyIds, List<Long> contributorIds, String fromDate, String toDate,
                                                     Pageable pageable);

    @Query(value = SELECT + """
            WHERE (
                r.recipient_id = :githubUserId
                OR
                (coalesce(:administratedBillingProfileIds) IS NOT NULL AND r.billing_profile_id IN (:administratedBillingProfileIds))
              )
              AND (coalesce(:currencyIds) IS NULL OR r.currency_id IN (:currencyIds))
              AND (coalesce(:projectIds) IS NULL OR r.project_id IN (:projectIds))
              AND (coalesce(:fromDate) IS NULL OR r.requested_at >= to_date(cast(:fromDate AS TEXT), 'YYYY-MM-DD'))
              AND (coalesce(:toDate) IS NULL OR r.requested_at < to_date(cast(:toDate AS TEXT), 'YYYY-MM-DD') + 1)
              AND (coalesce(:rewardStatus) is null or r.status = cast(:rewardStatus as accounting.reward_status))
            """, nativeQuery = true)
    Page<RewardDetailsReadEntity> findUserRewards(Long githubUserId, List<UUID> currencyIds, List<UUID> projectIds, List<UUID> administratedBillingProfileIds,
                                                  String rewardStatus, String fromDate, String toDate, Pageable pageable);
}
