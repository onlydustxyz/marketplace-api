package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardTotalAmountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class CustomUserRewardRepository {

    private final EntityManager entityManager;

    private static final String COUNT_USER_REWARDS = """
            select count(distinct pr.id)
            from auth_users au
                     join payment_requests pr on pr.recipient_id = au.github_user_id and au.id = :userId
            """;

    protected static final String FIND_USER_REWARDS_BY_ID = """
            select pr.requested_at,
                   pd.name,
                   pd.logo_url,
                   pd.project_id,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else coalesce(cuq.price, 0) * pr.amount end dollars_equivalent,
                   case
                       when payout_checks.has_pending_payments and
                            ((not payout_checks.valid_company and not payout_checks.valid_person)
                                or (not payout_checks.valid_apt_wallet or not payout_checks.valid_banking_account or not
                                    payout_checks.valid_eth_wallet
                                    or not payout_checks.valid_op_wallet or not payout_checks.valid_stark_wallet or not
                                        payout_checks.valid_usdc_wallet)) then 'MISSING_PAYOUT_INFO'
                       when pr.invoice_received_at is null then 'PENDING_INVOICE'
                       when r.id is not null then 'COMPLETE'
                       else 'PROCESSING'
                       end                                                                                  status
            from auth_users au
                     join payment_requests pr
                          on pr.recipient_id = au.github_user_id and au.id = :userId
                     join project_details pd on pd.project_id = pr.project_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     left join (select upi.user_id,
                                       (select count(pr.id) > 0
                                        from payment_requests pr
                                                 left join payments p on p.request_id = pr.id
                                        where p.id is null
                                          and pr.recipient_id = au.github_user_id)                      has_pending_payments,
                                       (upi.identity is not null and upi.identity -> 'Person' is not null and
                                        upi.identity -> 'Person' -> 'lastname' is not null and
                                        upi.identity -> 'Person' -> 'firstname' is not null)            valid_person,
                                       (upi.location is not null and upi.location -> 'city' is not null and
                                        upi.location -> 'post_code' is not null and
                                        upi.location -> 'address' is not null and
                                        upi.location -> 'country' is not null)                          valid_location,
                                       (upi.identity is not null and upi.identity -> 'Company' is not null and
                                        upi.identity -> 'Company' -> 'name' is not null and
                                        upi.identity -> 'Company' -> 'identification_number' is not null and
                                        upi.identity -> 'Company' -> 'owner' is not null and
                                        upi.identity -> 'Company' -> 'owner' -> 'firstname' is not null and
                                        upi.identity -> 'Company' -> 'owner' -> 'lastname' is not null) valid_company,
                                       coalesce((select w_op.address is not null
                                                 from payment_requests pr_op
                                                          left join payments p_op on p_op.request_id = pr_op.id
                                                          left join wallets w_op on w_op.user_id = upi.user_id and w_op.network = 'optimism'
                                                 where pr_op.currency = 'op'
                                                   and pr_op.recipient_id = au.github_user_id
                                                   and p_op is null
                                                 limit 1), true)                                        valid_op_wallet,
                                       coalesce((select w_stark.address is not null
                                                 from payment_requests pr_stark
                                                          left join payments p_stark on p_stark.request_id = pr_stark.id
                                                          left join wallets w_stark
                                                                    on w_stark.user_id = upi.user_id and w_stark.network = 'starknet'
                                                 where pr_stark.currency = 'stark'
                                                   and pr_stark.recipient_id = au.github_user_id
                                                   and p_stark is null
                                                 limit 1), true)                                        valid_stark_wallet,
                                       coalesce((select w_apt.address is not null
                                                 from payment_requests pr_apt
                                                          left join payments p_apt on p_apt.request_id = pr_apt.id
                                                          left join wallets w_apt on w_apt.user_id = upi.user_id and w_apt.network = 'aptos'
                                                 where pr_apt.currency = 'apt'
                                                   and pr_apt.recipient_id = au.github_user_id
                                                   and p_apt is null
                                                 limit 1), true)                                        valid_apt_wallet,
                                       case
                                           when (
                                               (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'fiat' and
                                                (select count(pr_usd.id) > 0
                                                 from payment_requests pr_usd
                                                          left join payments p_usd on p_usd.request_id = pr_usd.id
                                                 where pr_usd.recipient_id = au.github_user_id
                                                   and pr_usd.currency = 'usd'
                                                   and p_usd.id is null))
                                               ) then (select count(*) > 0
                                                       from bank_accounts ba
                                                       where ba.user_id = upi.user_id)
                                           else true end                                                valid_banking_account,
                                       case
                                           when (upi.identity -> 'Person' is not null) then (
                                               coalesce((select w_eth.address is not null
                                                         from payment_requests pr_usdc
                                                                  left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                                                  left join wallets w_eth
                                                                            on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                                         where pr_usdc.currency = 'usd'
                                                           and pr_usdc.recipient_id = au.github_user_id
                                                           and p_usdc is null
                                                         limit 1), true)
                                               )
                                           when (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'crypto')
                                               then (
                                               coalesce((select w_eth.address is not null
                                                         from payment_requests pr_usdc
                                                                  left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                                                  left join wallets w_eth
                                                                            on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                                         where pr_usdc.currency = 'usd'
                                                           and pr_usdc.recipient_id = au.github_user_id
                                                           and p_usdc is null
                                                         limit 1), true)
                                               )
                                           else true
                                           end                                                          valid_usdc_wallet,
                                       coalesce((select w_eth.address is not null
                                                 from payment_requests pr_eth
                                                          left join payments p_eth on p_eth.request_id = pr_eth.id
                                                          left join wallets w_eth
                                                                    on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                                 where pr_eth.currency = 'eth'
                                                   and pr_eth.recipient_id = au.github_user_id
                                                   and p_eth is null
                                                 limit 1), true)                                        valid_eth_wallet
                                from user_payout_info upi
                                         join auth_users au on upi.user_id = au.id) payout_checks
                               on payout_checks.user_id = au.id
            order by %order_by% offset :offset limit :limit
                     """;

    protected static final String FIND_USER_PENDING_INVOICE_REWARDS_BY_RECIPIENT_ID = """
            select pr.requested_at,
                   pd.name,
                   pd.logo_url,
                   pd.project_id,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else coalesce(cuq.price, 0) * pr.amount end dollars_equivalent,
                   'PENDING_INVOICE'                                                                        status,
                   (payout_checks.has_pending_payments and
                    ((not payout_checks.valid_company and not payout_checks.valid_person)
                        or (not payout_checks.valid_apt_wallet or not payout_checks.valid_banking_account or not
                            payout_checks.valid_eth_wallet
                            or not payout_checks.valid_op_wallet or not payout_checks.valid_stark_wallet or not
                                payout_checks.valid_usdc_wallet)))
            from payment_requests pr
                     join project_details pd on pd.project_id = pr.project_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     left join (select upi.user_id,
                                       (select count(pr.id) > 0
                                        from payment_requests pr
                                                 left join payments p on p.request_id = pr.id
                                        where p.id is null
                                          and pr.recipient_id = au.github_user_id)                      has_pending_payments,
                                       (upi.identity is not null and upi.identity -> 'Person' is not null and
                                        upi.identity -> 'Person' -> 'lastname' is not null and
                                        upi.identity -> 'Person' -> 'firstname' is not null)            valid_person,
                                       (upi.location is not null and upi.location -> 'city' is not null and
                                        upi.location -> 'post_code' is not null and
                                        upi.location -> 'address' is not null and
                                        upi.location -> 'country' is not null)                          valid_location,
                                       (upi.identity is not null and upi.identity -> 'Company' is not null and
                                        upi.identity -> 'Company' -> 'name' is not null and
                                        upi.identity -> 'Company' -> 'identification_number' is not null and
                                        upi.identity -> 'Company' -> 'owner' is not null and
                                        upi.identity -> 'Company' -> 'owner' -> 'firstname' is not null and
                                        upi.identity -> 'Company' -> 'owner' -> 'lastname' is not null) valid_company,
                                       coalesce((select w_op.address is not null
                                                 from payment_requests pr_op
                                                          left join payments p_op on p_op.request_id = pr_op.id
                                                          left join wallets w_op on w_op.user_id = upi.user_id and w_op.network = 'optimism'
                                                 where pr_op.currency = 'op'
                                                   and pr_op.recipient_id = au.github_user_id
                                                   and p_op is null
                                                 limit 1), true)                                        valid_op_wallet,
                                       coalesce((select w_stark.address is not null
                                                 from payment_requests pr_stark
                                                          left join payments p_stark on p_stark.request_id = pr_stark.id
                                                          left join wallets w_stark
                                                                    on w_stark.user_id = upi.user_id and w_stark.network = 'starknet'
                                                 where pr_stark.currency = 'stark'
                                                   and pr_stark.recipient_id = au.github_user_id
                                                   and p_stark is null
                                                 limit 1), true)                                        valid_stark_wallet,
                                       coalesce((select w_apt.address is not null
                                                 from payment_requests pr_apt
                                                          left join payments p_apt on p_apt.request_id = pr_apt.id
                                                          left join wallets w_apt on w_apt.user_id = upi.user_id and w_apt.network = 'aptos'
                                                 where pr_apt.currency = 'apt'
                                                   and pr_apt.recipient_id = au.github_user_id
                                                   and p_apt is null
                                                 limit 1), true)                                        valid_apt_wallet,
                                       case
                                           when (
                                               (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'fiat' and
                                                (select count(pr_usd.id) > 0
                                                 from payment_requests pr_usd
                                                          left join payments p_usd on p_usd.request_id = pr_usd.id
                                                 where pr_usd.recipient_id = au.github_user_id
                                                   and pr_usd.currency = 'usd'
                                                   and p_usd.id is null))
                                               ) then (select count(*) > 0
                                                       from bank_accounts ba
                                                       where ba.user_id = upi.user_id)
                                           else true end                                                valid_banking_account,
                                       case
                                           when (upi.identity -> 'Person' is not null) then (
                                               coalesce((select w_eth.address is not null
                                                         from payment_requests pr_usdc
                                                                  left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                                                  left join wallets w_eth
                                                                            on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                                         where pr_usdc.currency = 'usd'
                                                           and pr_usdc.recipient_id = au.github_user_id
                                                           and p_usdc is null
                                                         limit 1), true)
                                               )
                                           when (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'crypto')
                                               then (
                                               coalesce((select w_eth.address is not null
                                                         from payment_requests pr_usdc
                                                                  left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                                                  left join wallets w_eth
                                                                            on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                                         where pr_usdc.currency = 'usd'
                                                           and pr_usdc.recipient_id = au.github_user_id
                                                           and p_usdc is null
                                                         limit 1), true)
                                               )
                                           else true
                                           end                                                          valid_usdc_wallet,
                                       coalesce((select w_eth.address is not null
                                                 from payment_requests pr_eth
                                                          left join payments p_eth on p_eth.request_id = pr_eth.id
                                                          left join wallets w_eth
                                                                    on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                                 where pr_eth.currency = 'eth'
                                                   and pr_eth.recipient_id = au.github_user_id
                                                   and p_eth is null
                                                 limit 1), true)                                        valid_eth_wallet,
                                       au.github_user_id
                                from user_payout_info upi
                                         join auth_users au on upi.user_id = au.id) payout_checks
                               on payout_checks.github_user_id = pr.recipient_id
            where pr.recipient_id = :recipientId
              and not (payout_checks.has_pending_payments and
                       ((not payout_checks.valid_company and not payout_checks.valid_person)
                           or (not payout_checks.valid_apt_wallet or not payout_checks.valid_banking_account or not
                               payout_checks.valid_eth_wallet
                               or not payout_checks.valid_op_wallet or not payout_checks.valid_stark_wallet or not
                                   payout_checks.valid_usdc_wallet)))
              and pr.invoice_received_at is null
                     """;


    public Integer getCount(UUID userId) {
        final var query = entityManager
                .createNativeQuery(COUNT_USER_REWARDS)
                .setParameter("userId", userId);
        return ((Number) query.getSingleResult()).intValue();
    }

    public List<UserRewardViewEntity> getViewEntities(UUID userId, UserRewardView.SortBy sortBy,
                                                      SortDirection sortDirection, int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(buildQuery(sortBy, sortDirection), UserRewardViewEntity.class)
                .setParameter("userId", userId)
                .setParameter("offset", PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex))
                .setParameter("limit", PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex))
                .getResultList();
    }

    protected static String buildQuery(UserRewardView.SortBy sortBy, final SortDirection sortDirection) {
        sortBy = isNull(sortBy) ? UserRewardView.SortBy.requestedAt : sortBy;
        final String sort = switch (sortBy) {
            case amount -> "dollars_equivalent " + sortDirection.name() + ", requested_at desc";
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
            from auth_users au
                     join payment_requests pr
                          on pr.recipient_id = au.github_user_id and au.id = :userId
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
