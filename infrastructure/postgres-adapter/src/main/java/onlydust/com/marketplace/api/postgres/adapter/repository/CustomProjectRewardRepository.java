package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.intellij.lang.annotations.Language;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class CustomProjectRewardRepository {

    @Language("PostgreSQL")
    protected static final String FIND_PROJECT_REWARDS = """
                with billing_profile_check as (select ubpt.user_id,
                                                     (
                                                         case
                                                             when ubpt.billing_profile_type = 'INDIVIDUAL'
                                                                 then ibp.verification_status = 'VERIFIED'
                                                             when ubpt.billing_profile_type = 'COMPANY'
                                                                 then cbp.verification_status = 'VERIFIED'
                                                             else false
                                                             end
                                                         ) billing_profile_verified
                                              from user_billing_profile_types ubpt
                                                       left join individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                                                       left join company_billing_profiles cbp on cbp.user_id = ubpt.user_id),
                    payout_checks as (select u.github_user_id                           github_user_id,
                                             coalesce(wallets.list, '{}')               wallets,
                                             ba.iban is not null and ba.bic is not null has_bank_account
                                      from iam.users u
                                               left join public.user_payout_info upi on u.id = upi.user_id
                                               left join (select w.user_id, array_agg(distinct w.network) as list
                                                          from wallets w
                                                          group by w.user_id) wallets on wallets.user_id = u.id
                                               left join bank_accounts ba on ba.user_id = u.id)
               select pr.requested_at,
                      r.processed_at,
                      gu.login,
                      user_avatar_url(gu.id, gu.avatar_url) as avatar_url,
                      pr.id,
                      pr.amount,
                      pr.currency,
                      (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                      case when pr.currency = 'usd' then pr.amount else cuq.price * pr.amount end dollars_equivalent,
                      case
                          when u.id is null then 'PENDING_SIGNUP'
                          when not coalesce(bpc.billing_profile_verified, false) then 'PENDING_CONTRIBUTOR'
                          when (case
                                    when pr.currency in ('eth', 'lords', 'usdc')
                                        then not payout_checks.wallets @> array [cast('ethereum' as network)]
                                    when pr.currency = 'strk' then not payout_checks.wallets @> array [cast('starknet' as network)]
                                    when pr.currency = 'op' then not payout_checks.wallets @> array [cast('optimism' as network)]
                                    when pr.currency = 'apt' then not payout_checks.wallets @> array [cast('aptos' as network)]
                                    when pr.currency = 'usd' then not payout_checks.has_bank_account
                              end) then 'PENDING_CONTRIBUTOR'
                          when r.id is not null then 'COMPLETE'
                          when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') THEN 'LOCKED'
                          else 'PROCESSING'
                          end                                          status
               from payment_requests pr
                        join indexer_exp.github_accounts gu on gu.id = pr.recipient_id
                        left join iam.users u on gu.id = u.github_user_id
                        left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                        left join payments r on r.request_id = pr.id
                        left join billing_profile_check bpc on bpc.user_id = u.id
                        LEFT JOIN payout_checks ON payout_checks.github_user_id = pr.recipient_id
               where pr.project_id = :projectId
                 and (coalesce(:currencies) is null or CAST(pr.currency AS TEXT) IN (:currencies))
                 and (coalesce(:contributorsIds) is null or pr.recipient_id in (:contributorsIds))
                 and (:fromDate IS NULL OR pr.requested_at >= to_date(cast(:fromDate as text), 'YYYY-MM-DD'))
                 AND (:toDate IS NULL OR pr.requested_at < to_date(cast(:toDate as text), 'YYYY-MM-DD') + 1)
               order by %order_by%
               offset :offset limit :limit
            """;
    private static final String COUNT_PROJECT_REWARDS = """
            select count(*)
            from payment_requests pr
            where
                pr.project_id = :projectId and 
                (coalesce(:currencies) is null or CAST(pr.currency AS TEXT) IN (:currencies)) and 
                (coalesce(:contributorsIds) is null or pr.recipient_id in (:contributorsIds)) AND
                (:fromDate IS NULL OR pr.requested_at >= to_date(cast(:fromDate as text), 'YYYY-MM-DD')) AND
                (:toDate IS NULL OR pr.requested_at < to_date(cast(:toDate as text), 'YYYY-MM-DD') + 1)
            """;
    private final EntityManager entityManager;

    protected static String buildQuery(ProjectRewardView.SortBy sortBy, final SortDirection sortDirection) {
        sortBy = isNull(sortBy) ? ProjectRewardView.SortBy.requestedAt : sortBy;
        final String sort = switch (sortBy) {
            case amount -> "dollars_equivalent " + sortDirection.name() + ", requested_at desc";
            case contribution -> "contribution_count " + sortDirection.name() + ", requested_at desc";
            case status -> "status " + sortDirection.name() + ", requested_at desc";
            default -> "requested_at " + sortDirection.name();
        };
        return FIND_PROJECT_REWARDS.replace("%order_by%", sort);
    }

    public Integer getCount(UUID projectId, List<String> currencies, List<Long> contributorsIds, String from,
                            String to) {
        final var query = entityManager
                .createNativeQuery(COUNT_PROJECT_REWARDS)
                .setParameter("projectId", projectId)
                .setParameter("currencies", currencies)
                .setParameter("contributorsIds", contributorsIds)
                .setParameter("fromDate", from)
                .setParameter("toDate", to);
        return ((Number) query.getSingleResult()).intValue();
    }

    public List<ProjectRewardViewEntity> getViewEntities(UUID projectId, List<String> currencies,
                                                         List<Long> contributorsIds,
                                                         String from, String to,
                                                         ProjectRewardView.SortBy sortBy,
                                                         final SortDirection sortDirection,
                                                         int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(buildQuery(sortBy, sortDirection), ProjectRewardViewEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("currencies", currencies)
                .setParameter("contributorsIds", contributorsIds)
                .setParameter("fromDate", from)
                .setParameter("toDate", to)
                .setParameter("offset", PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex))
                .setParameter("limit", PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex))
                .getResultList();
    }
}
