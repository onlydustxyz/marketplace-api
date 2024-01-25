package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardTotalAmountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import org.intellij.lang.annotations.Language;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class CustomUserRewardRepository {

    private final EntityManager entityManager;

    private static final String COUNT_USER_REWARDS = """
            select count(distinct pr.id)
            from iam.users u
                     join payment_requests pr on pr.recipient_id = u.github_user_id and
                     u.id = :userId and
                     (coalesce(:currencies) is null or CAST(pr.currency AS TEXT) in (:currencies)) and
                     (coalesce(:projectIds) is null or pr.project_id in (:projectIds)) and
                     (:fromDate IS NULL OR pr.requested_at >= to_date(cast(:fromDate as text), 'YYYY-MM-DD')) AND
                     (:toDate IS NULL OR pr.requested_at < to_date(cast(:toDate as text), 'YYYY-MM-DD') + 1)
            """;

    @Language("PostgreSQL")
    protected static final String FIND_USER_REWARDS_BY_ID = """
                with payout_checks as (select u.id                                                                        user_id,
                                            (upi.identity is not null and upi.identity -> 'Person' is not null and
                                             upi.identity -> 'Person' -> 'lastname' != cast('null' as jsonb) and
                                             upi.identity -> 'Person' -> 'firstname' != cast('null' as jsonb))            valid_person,
                                            (upi.location is not null and upi.location -> 'city' != cast('null' as jsonb) and
                                             upi.location -> 'post_code' != cast('null' as jsonb) and
                                             upi.location -> 'address' != cast('null' as jsonb) and
                                             upi.location -> 'country' != cast('null' as jsonb))                          valid_location,
                                            (upi.identity is not null and upi.identity -> 'Company' is not null and
                                             upi.identity -> 'Company' -> 'name' != cast('null' as jsonb) and
                                             upi.identity -> 'Company' -> 'identification_number' != cast('null' as jsonb) and
                                             upi.identity -> 'Company' -> 'owner' is not null and
                                             upi.identity -> 'Company' -> 'owner' -> 'firstname' != cast('null' as jsonb) and
                                             upi.identity -> 'Company' -> 'owner' -> 'lastname' != cast('null' as jsonb)) valid_company,
                                             coalesce(wallets.list, '{}')                                                 wallets,
                                             ba.iban is not null and ba.bic is not null                                   has_bank_account
                                       from iam.users u
                                                left join public.user_payout_info upi on u.id = upi.user_id
                                                left join (select w.user_id, array_agg(distinct w.network) as list
                                                           from wallets w
                                                           group by w.user_id) wallets on wallets.user_id = u.id
                                                left join bank_accounts ba on ba.user_id = u.id)
                select pr.requested_at,
                       pd.name,
                       pd.logo_url,
                       pd.project_id,
                       pr.id,
                       pr.amount,
                       pr.currency,
                       pr.invoice_received_at,
                       (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                       case when pr.currency = 'usd' then pr.amount else cuq.price * pr.amount end dollars_equivalent,
                       
                       case
                           when r.id is not null then 'COMPLETE'
                           when pr.currency = 'strk' THEN 'LOCKED'
                           when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') THEN 'LOCKED'
                           when not payout_checks.valid_location or
                                (not payout_checks.valid_company and not payout_checks.valid_person) or
                                (case
                                     when pr.currency in ('eth','lords','usdc') then not payout_checks.wallets @> array[cast('ethereum' as network)]
                                     when pr.currency = 'strk' then not payout_checks.wallets @> array[cast('starknet' as network)]
                                     when pr.currency = 'op' then not payout_checks.wallets @> array[cast('optimism' as network)]
                                     when pr.currency = 'apt' then not payout_checks.wallets @> array[cast('aptos' as network)]
                                     when pr.currency = 'usd' then not payout_checks.has_bank_account
                                end) then 'MISSING_PAYOUT_INFO'
                           when payout_checks.valid_company and pr.invoice_received_at is null then 'PENDING_INVOICE'
                           else 'PROCESSING'
                           end                                                                                  status
                from iam.users u
                         join payment_requests pr
                              on pr.recipient_id = u.github_user_id
                         join project_details pd on pd.project_id = pr.project_id
                         left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                         left join payments r on r.request_id = pr.id
                         left join payout_checks on payout_checks.user_id = u.id
                where u.id = :userId and
                     (coalesce(:currencies) is null or CAST(pr.currency AS TEXT) in (:currencies)) and
                     (coalesce(:projectIds) is null or pr.project_id in (:projectIds)) and
                     (:fromDate IS NULL OR pr.requested_at >= to_date(cast(:fromDate as text), 'YYYY-MM-DD')) AND
                     (:toDate IS NULL OR pr.requested_at < to_date(cast(:toDate as text), 'YYYY-MM-DD') + 1)
            order by %order_by% offset :offset limit :limit
                     """;

    protected static final String FIND_USER_PENDING_INVOICE_REWARDS_BY_RECIPIENT_ID = """
                with payout_checks as (select u.id                                                                        user_id,
                                            (upi.identity is not null and upi.identity -> 'Person' is not null and
                                             upi.identity -> 'Person' -> 'lastname' != cast('null' as jsonb) and
                                             upi.identity -> 'Person' -> 'firstname' != cast('null' as jsonb))            valid_person,
                                            (upi.location is not null and upi.location -> 'city' != cast('null' as jsonb) and
                                             upi.location -> 'post_code' != cast('null' as jsonb) and
                                             upi.location -> 'address' != cast('null' as jsonb) and
                                             upi.location -> 'country' != cast('null' as jsonb))                          valid_location,
                                            (upi.identity is not null and upi.identity -> 'Company' is not null and
                                             upi.identity -> 'Company' -> 'name' != cast('null' as jsonb) and
                                             upi.identity -> 'Company' -> 'identification_number' != cast('null' as jsonb) and
                                             upi.identity -> 'Company' -> 'owner' is not null and
                                             upi.identity -> 'Company' -> 'owner' -> 'firstname' != cast('null' as jsonb) and
                                             upi.identity -> 'Company' -> 'owner' -> 'lastname' != cast('null' as jsonb)) valid_company,
                                             coalesce(wallets.list, '{}')                                                 wallets,
                                             ba.iban is not null and ba.bic is not null                                   has_bank_account
                                       from iam.users u
                                                left join public.user_payout_info upi on u.id = upi.user_id
                                                left join (select w.user_id, array_agg(distinct w.network) as list
                                                           from wallets w
                                                           group by w.user_id) wallets on wallets.user_id = u.id
                                                left join bank_accounts ba on ba.user_id = u.id)
            select distinct pr.requested_at,
                   pd.name,
                   pd.logo_url,
                   pd.project_id,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   pr.invoice_received_at,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else coalesce(cuq.price, 0) * pr.amount end dollars_equivalent,
                   'PENDING_INVOICE'                                                                                  status
            from payment_requests pr
                     join iam.users u
                          on pr.recipient_id = u.github_user_id
                     join project_details pd on pd.project_id = pr.project_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     left join payout_checks on payout_checks.user_id = u.id
            where pr.recipient_id = :recipientId
                    and (case
                           when r.id is not null then 'COMPLETE'
                           when pr.currency = 'strk' THEN 'LOCKED'
                           when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') THEN 'LOCKED'
                           when not payout_checks.valid_location or
                                (not payout_checks.valid_company and not payout_checks.valid_person) or
                                (case
                                     when pr.currency in ('eth','lords','usdc') then not payout_checks.wallets @> array[cast('ethereum' as network)]
                                     when pr.currency = 'strk' then not payout_checks.wallets @> array[cast('starknet' as network)]
                                     when pr.currency = 'op' then not payout_checks.wallets @> array[cast('optimism' as network)]
                                     when pr.currency = 'apt' then not payout_checks.wallets @> array[cast('aptos' as network)]
                                     when pr.currency = 'usd' then not payout_checks.has_bank_account
                                end) then 'MISSING_PAYOUT_INFO'
                           when payout_checks.valid_company and pr.invoice_received_at is null then 'PENDING_INVOICE'
                           else 'PROCESSING'
                           end) = 'PENDING_INVOICE'
                     """;


    public Integer getCount(UUID userId, List<String> currencies, List<UUID> projectIds, String fromDate,
                            String toDate) {
        final var query = entityManager
                .createNativeQuery(COUNT_USER_REWARDS)
                .setParameter("userId", userId)
                .setParameter("currencies", currencies)
                .setParameter("projectIds", projectIds)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);
        return ((Number) query.getSingleResult()).intValue();
    }

    public List<UserRewardViewEntity> getViewEntities(UUID userId, List<String> currencies, List<UUID> projectIds,
                                                      String fromDate, String toDate,
                                                      UserRewardView.SortBy sortBy,
                                                      SortDirection sortDirection, int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(buildQuery(sortBy, sortDirection), UserRewardViewEntity.class)
                .setParameter("userId", userId)
                .setParameter("currencies", currencies)
                .setParameter("projectIds", projectIds)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("offset", PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex))
                .setParameter("limit", PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex))
                .getResultList();
    }

    protected static String buildQuery(UserRewardView.SortBy sortBy, final SortDirection sortDirection) {
        sortBy = isNull(sortBy) ? UserRewardView.SortBy.requestedAt : sortBy;
        final String sort = switch (sortBy) {
            case amount -> "dollars_equivalent " + sortDirection.name() + " nulls last, requested_at desc";
            case contribution -> "contribution_count " + sortDirection.name() + ", requested_at desc";
            case status -> "status " + sortDirection.name() + ", requested_at desc";
            default -> "requested_at " + sortDirection.name();
        };
        return FIND_USER_REWARDS_BY_ID.replace("%order_by%", sort);
    }


    private static final String FIND_USER_REWARD_TOTAL_AMOUNTS_BY_USER_ID = """
            select row_number() over (order by pr.currency) id,
                   pr.currency,
                   sum(pr.amount) total,
                   case
                       when pr.currency = 'usd' then sum(pr.amount)
                       else (select price from crypto_usd_quotes cuq where cuq.currency = pr.currency) *
                            sum(pr.amount) end dollars_equivalent
            from iam.users u
                     join payment_requests pr
                          on pr.recipient_id = u.github_user_id and u.id = :userId
            group by pr.currency""";


    public List<UserRewardTotalAmountEntity> getTotalAmountEntities(UUID userId) {
        return entityManager.createNativeQuery(FIND_USER_REWARD_TOTAL_AMOUNTS_BY_USER_ID,
                        UserRewardTotalAmountEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<UserRewardViewEntity> getPendingInvoicesViewEntities(final Long recipientId) {
        return entityManager.createNativeQuery(FIND_USER_PENDING_INVOICE_REWARDS_BY_RECIPIENT_ID,
                        UserRewardViewEntity.class)
                .setParameter("recipientId", recipientId)
                .getResultList();
    }
}
