package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.project.domain.model.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
                   r.invoice_id                 AS invoice_id,
                   r.billing_profile_id         AS billing_profile_id,
                   count(*)                     AS contribution_count,
                   github_recipient.id          AS recipient_id,
                   github_recipient.login       AS recipient_login,
                   user_avatar_url(r.recipient_id, github_recipient.avatar_url)  AS recipient_avatar_url,
                   github_requestor.id          AS requestor_id,
                   github_requestor.login       AS requestor_login,
                   user_avatar_url(github_requestor.id, github_requestor.avatar_url)  AS requestor_avatar_url
            from rewards r
                 JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                 JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
                 JOIN reward_items ri ON ri.reward_id = r.id
                 JOIN indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id
                 JOIN iam.users requestor ON requestor.id = r.requestor_id
                 JOIN indexer_exp.github_accounts github_requestor ON github_requestor.id = requestor.github_user_id
            WHERE (coalesce(:rewardIds) IS NULL OR r.id IN (:rewardIds))
              AND (coalesce(:statuses) IS NULL OR CAST(rs.status AS TEXT) IN (:statuses))
              AND (
                    (:companyBillingProfileAdminIds IS NULL AND (coalesce(:contributorIds) IS NULL OR r.recipient_id IN (:contributorIds)))
                    OR
                    (:companyBillingProfileAdminIds IS NOT NULL AND
                        (
                            (r.billing_profile_id IN (:companyBillingProfileAdminIds) AND r.recipient_id NOT IN (:contributorIds))
                            OR
                            r.recipient_id IN (:contributorIds)
                        )
                    )
                  )
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
                r.billing_profile_id,
                rsd.amount_usd_equivalent,
                github_recipient.id,
                github_recipient.login,
                github_recipient.avatar_url,
                github_requestor.id,
                github_requestor.login,
                github_requestor.avatar_url
            """, nativeQuery = true)
    Page<RewardViewEntity> find(List<UUID> rewardIds,
                                List<Long> contributorIds,
                                List<UUID> currencyIds,
                                List<UUID> projectIds,
                                List<String> statuses,
                                List<UUID> companyBillingProfileAdminIds,
                                String fromDate,
                                String toDate,
                                Pageable pageable);

    static Sort sortBy(final Reward.SortBy sortBy, final Sort.Direction direction) {
        return switch (sortBy) {
            case AMOUNT -> JpaSort.unsafe(direction, "coalesce(rsd.amount_usd_equivalent, 0)").and(Sort.by("requested_at").descending());
            case CONTRIBUTION -> Sort.by(direction, "contribution_count").and(Sort.by("requested_at").descending());
            case STATUS -> Sort.by(direction, "rs.status").and(Sort.by("requested_at").descending());
            case REQUESTED_AT -> Sort.by(direction, "requested_at");
        };
    }


    default Optional<RewardViewEntity> find(UUID rewardId) {
        return find(
                List.of(rewardId),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                Pageable.unpaged())
                .stream().findFirst();
    }

    default List<RewardViewEntity> findPendingPaymentRequestForRecipient(Long githubUserId) {
        return find(
                List.of(),
                List.of(githubUserId),
                List.of(),
                List.of(),
                List.of(RewardStatusEntity.Status.PENDING_REQUEST.name()),
                List.of(),
                null,
                null,
                Pageable.unpaged())
                .getContent();
    }


    default Page<RewardViewEntity> findProjectRewards(UUID projectId, List<UUID> currencies, List<Long> contributors, String fromDate, String toDate,
                                                      PageRequest pageRequest) {
        return find(
                List.of(),
                contributors,
                currencies,
                List.of(projectId),
                List.of(),
                List.of(),
                fromDate,
                toDate,
                pageRequest);
    }

    default Page<RewardViewEntity> findUserRewards(Long githubUserId, List<UUID> currencies, List<UUID> projectIds, List<UUID> adminCompanyBillingProfilesIds,
                                                   String fromDate, String toDate, PageRequest pageRequest) {
        return find(
                List.of(),
                List.of(githubUserId),
                currencies,
                projectIds,
                List.of(),
                adminCompanyBillingProfilesIds,
                fromDate,
                toDate,
                pageRequest);
    }

    @Modifying
    @Query(nativeQuery = true, value = """
            update rewards
            set payment_notified_at = now()
            where id in (:rewardIds)
            """)
    void markRewardAsPaymentNotified(List<UUID> rewardIds);
}
