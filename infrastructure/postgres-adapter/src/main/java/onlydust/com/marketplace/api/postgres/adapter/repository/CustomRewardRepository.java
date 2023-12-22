package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardItemViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class CustomRewardRepository {
    private static final String FIND_PROJECT_REWARD_BY_ID = """
            select pr.requested_at,
                   r.processed_at,
                   gu_recipient.login                                                                       recipient_login,
                   user_avatar_url(gu_recipient.id, gu_recipient.avatar_url)                                recipient_avatar_url,
                   gu_recipient.id                                                                          recipient_id,
                   gu_requestor.login                                                                       requestor_login,
                   user_avatar_url(gu_requestor.id, gu_requestor.avatar_url)                                requestor_avatar_url,
                   gu_requestor.id                                                                          requestor_id,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else cuq.price * pr.amount end dollars_equivalent,
                   case
                       when u.id is null then 'PENDING_SIGNUP'
                       when r.id is not null then 'COMPLETE'
                       else 'PROCESSING'
                       end                                                                                  status,
                       r.receipt,
                    p.project_id,
                    p.key as project_key,
                    p.name as project_name,
                    p.short_description as project_short_description,
                    p.long_description as project_long_description,
                    p.logo_url as project_logo_url,
                    p.telegram_link as project_telegram_link,
                    p.hiring as project_hiring,
                    p.visibility as project_visibility
            from payment_requests pr
                     join project_details p on p.project_id = pr.project_id
                     left join indexer_exp.github_accounts gu_recipient on gu_recipient.id = pr.recipient_id
                     left join iam.users u on pr.recipient_id = u.github_user_id
                     left join iam.users u_requestor on u_requestor.id = pr.requestor_id
                     left join indexer_exp.github_accounts gu_requestor on gu_requestor.id = u_requestor.github_user_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     where pr.id = :rewardId""";
    private static final String FIND_USER_REWARD_BY_ID = """
            with payout_checks as (select pr.id,
                                          pr.recipient_id,
                                          u.id                                                            user_id,
                                          (select count(p.id) > 0
                                           from payment_requests pr2
                                                    left join payments p on p.request_id = pr2.id
                                           where p.id is null
                                             and pr.id = pr2.id)                                           has_pending_payments,
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
                                          coalesce((select w_eth.address is not null
                                                    from payment_requests pr_eth
                                                             left join payments p_eth on p_eth.request_id = pr_eth.id
                                                             left join wallets w_eth
                                                                       on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                                    where pr_eth.currency in ('eth','lords')
                                                      and pr_eth.id = pr.id
                                                      and pr_eth.recipient_id = u.github_user_id
                                                      and p_eth is null
                                                    limit 1), true)                                        valid_eth_wallet,
                                          coalesce((select w_op.address is not null
                                                    from payment_requests pr_op
                                                             left join payments p_op on p_op.request_id = pr_op.id
                                                             left join wallets w_op on w_op.user_id = upi.user_id and w_op.network = 'optimism'
                                                    where pr_op.currency = 'op'
                                                      and pr_op.id = pr.id
                                                      and pr_op.recipient_id = u.github_user_id
                                                      and p_op is null
                                                    limit 1), true)                                        valid_op_wallet,
                                          coalesce((select w_stark.address is not null
                                                    from payment_requests pr_stark
                                                             left join payments p_stark on p_stark.request_id = pr_stark.id
                                                             left join wallets w_stark
                                                                       on w_stark.user_id = upi.user_id and w_stark.network = 'starknet'
                                                    where pr_stark.currency = 'strk'
                                                      and pr_stark.id = pr.id
                                                      and pr_stark.recipient_id = u.github_user_id
                                                      and p_stark is null
                                                    limit 1), true)                                        valid_stark_wallet,
                                          coalesce((select w_apt.address is not null
                                                    from payment_requests pr_apt
                                                             left join payments p_apt on p_apt.request_id = pr_apt.id
                                                             left join wallets w_apt on w_apt.user_id = upi.user_id and w_apt.network = 'aptos'
                                                    where pr_apt.currency = 'apt'
                                                      and pr_apt.recipient_id = u.github_user_id
                                                      and p_apt is null
                                                    limit 1), true)                                        valid_apt_wallet,
                                          case
                                              when (
                                                  (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'fiat' and
                                                   (select count(pr_usd.id) > 0
                                                    from payment_requests pr_usd
                                                             left join payments p_usd on p_usd.request_id = pr_usd.id
                                                    where pr_usd.recipient_id = u.github_user_id
                                                      and pr_usd.id = pr.id
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
                                                              and pr_usdc.id = pr.id
                                                              and pr_usdc.recipient_id = u.github_user_id
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
                                                              and pr_usdc.id = pr.id
                                                              and pr_usdc.recipient_id = u.github_user_id
                                                              and p_usdc is null
                                                            limit 1), true)
                                                  )
                                              else true
                                              end                                                          valid_usdc_wallet
                                   from payment_requests pr
                                            left join iam.users u on u.github_user_id = pr.recipient_id
                                            left join public.user_payout_info upi on u.id = upi.user_id)
            select pr.requested_at,
                   r.processed_at,
                   gu_recipient.login                                                                       recipient_login,
                   user_avatar_url(gu_recipient.id, gu_recipient.avatar_url)                                recipient_avatar_url,
                   gu_recipient.id                                                                          recipient_id,
                   gu_requestor.login                                                                       requestor_login,
                   user_avatar_url(gu_requestor.id, gu_requestor.avatar_url)                                requestor_avatar_url,
                   gu_requestor.id                                                                          requestor_id,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else cuq.price * pr.amount end dollars_equivalent,
                   case
                       when r.id is not null then 'COMPLETE'
                       when (case
                                 when pr.currency in ('eth','lords') then
                                         (not payout_checks.valid_company and not payout_checks.valid_person) or
                                         not payout_checks.valid_eth_wallet
                                 when pr.currency = 'strk' then
                                         (not payout_checks.valid_company and not payout_checks.valid_person) or
                                         not payout_checks.valid_stark_wallet
                                 when pr.currency = 'op' then
                                         (not payout_checks.valid_company and not payout_checks.valid_person) or
                                         not payout_checks.valid_op_wallet
                                 when pr.currency = 'apt' then
                                         (not payout_checks.valid_company and not payout_checks.valid_person) or
                                         not payout_checks.valid_apt_wallet
                                 when pr.currency = 'usd' then (
                                         (not payout_checks.valid_company and not payout_checks.valid_person)
                                         or (not payout_checks.valid_usdc_wallet or not payout_checks.valid_banking_account)
                                     )
                           end) then 'MISSING_PAYOUT_INFO'
                       when payout_checks.valid_company and pr.invoice_received_at is null then 'PENDING_INVOICE'
                       else 'PROCESSING'
                       end                                                                                  status,
                       r.receipt,
                       p.project_id,
                       p.key as project_key,
                       p.name as project_name,
                       p.short_description as project_short_description,
                       p.long_description as project_long_description,
                       p.logo_url as project_logo_url,
                       p.telegram_link as project_telegram_link,
                       p.hiring as project_hiring,
                       p.visibility as project_visibility
            from payment_requests pr
                     join project_details p on p.project_id = pr.project_id
                     left join indexer_exp.github_accounts gu_recipient on gu_recipient.id = pr.recipient_id
                     left join iam.users u on pr.recipient_id = u.github_user_id
                     left join iam.users u_requestor on u_requestor.id = pr.requestor_id
                     left join indexer_exp.github_accounts gu_requestor on gu_requestor.id = u_requestor.github_user_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     left join payout_checks on payout_checks.user_id = u.id and payout_checks.id = pr.id
                     where pr.id = :rewardId""";
    private static final String COUNT_REWARD_ITEMS = """
            select count(distinct wi.id)
            from payment_requests pr
                     join public.work_items wi on wi.payment_id = pr.id
            where pr.id = :rewardId""";
    private static final String FIND_REWARD_ITEMS = """
            with get_pr as              (select gpr.number,
                                               gpr.id,
                                               gpr.html_url,
                                               gpr.title,
                                               gpr.status,
                                               gpr.draft,
                                               gpr.repo_name                                repo_name,
                                               gpr.created_at                               start_date,
                                               coalesce(gpr.closed_at, gpr.merged_at)       end_date,
                                               gpr.author_id                                author_id,
                                               gpr.author_login                             author_login,
                                               user_avatar_url(gpr.author_id, gpr.author_avatar_url) author_avatar_url,
                                               gpr.author_html_url                          author_github_url,
                                               gpr.commit_count                             commits_count
                                        from indexer_exp.github_pull_requests gpr),
                 get_issue as           (select gi.number,
                                              gi.id,
                                              gi.status,
                                              gi.html_url,
                                              gi.title,
                                              gi.repo_name       repo_name,
                                              gi.created_at start_date,
                                              gi.closed_at  end_date,
                                              gi.author_id         author_id,
                                              gi.author_login      author_login,
                                              user_avatar_url(gi.author_id, gi.author_avatar_url) author_avatar_url,
                                              gi.author_html_url   author_github_url,
                                              gi.comments_count
                                       from indexer_exp.github_issues gi),
                 get_code_review as    (select gprr.number,
                                              gprr.id,
                                              gprr.state status,
                                              gprr.html_url,
                                              gprr.title,
                                              gprr.state outcome,
                                              gprr.repo_name             repo_name,
                                              gprr.requested_at          start_date,
                                              gprr.submitted_at          end_date,
                                              gprr.author_id             author_id,
                                              gprr.author_login          author_login,
                                              user_avatar_url(gprr.author_id, gprr.author_avatar_url)     author_avatar_url,
                                              gprr.author_html_url       author_github_url
                                       from indexer_exp.github_code_reviews gprr)
            select distinct wi.type,
                            coalesce(cast(pull_request.id as text), cast(issue.id as text), cast(code_review.id as text))             reward_id,
                            c.id                                                                                                      contribution_id,
                            coalesce(cast(pull_request.status as text), cast(issue.status as text), cast(code_review.status as text)) status,
                            coalesce(pull_request.number, issue.number, code_review.number)                   number,
                            coalesce(pull_request.html_url, issue.html_url, code_review.html_url)             html_url,
                            coalesce(pull_request.title, issue.title, code_review.title)                      title,
                            coalesce(pull_request.repo_name, issue.repo_name, code_review.repo_name)          repo_name,
                            coalesce(pull_request.start_date, issue.start_date, code_review.start_date)       start_date,
                            coalesce(pull_request.end_date, issue.end_date, code_review.end_date)             end_date,
                            coalesce(pull_request.author_id, issue.author_id, code_review.author_id)          author_id,
                            coalesce(pull_request.author_login, issue.author_login, code_review.author_login) author_login,
                            user_avatar_url(coalesce(pull_request.author_id, issue.author_id, code_review.author_id),
                                            coalesce(pull_request.author_avatar_url, issue.author_avatar_url, code_review.author_avatar_url))       author_avatar_url,
                            coalesce(pull_request.html_url, issue.html_url, code_review.html_url)             html_url,
                            coalesce(pull_request.author_github_url, issue.author_github_url, code_review.author_github_url) author_github_url,
                            pull_request.commits_count,
                            (select gprcc.commit_count
                             from indexer_exp.github_pull_request_commit_counts gprcc
                             where gprcc.pull_request_id = pull_request.id and gprcc.author_id = pr.recipient_id) user_commits_count,
                            issue.comments_count,
                            pr.recipient_id
            from payment_requests pr
                     join public.work_items wi on wi.payment_id = pr.id
                     left join get_issue issue on issue.id = (case when wi.id ~ '^[0-9]+$' then cast(wi.id as bigint) else -1 end)
                     left join get_pr pull_request on pull_request.id = (case when wi.id ~ '^[0-9]+$' then cast(wi.id as bigint) else -1 end)
                     left join get_code_review code_review on code_review.id = wi.id
                     left join indexer_exp.contributions c on c.contributor_id = pr.recipient_id and c.repo_id = wi.repo_id and
                                                              -- check that the repo is still part of the project (if it's not, then the contribution doesn't belong to the project)
                                                              exists (select 1 from project_github_repos pgr where pgr.github_repo_id = c.repo_id and pgr.project_id = pr.project_id) and
                                                              ((issue.id IS NOT NULL and c.issue_id = issue.id) or
                                                              (pull_request.id IS NOT NULL and c.pull_request_id = pull_request.id) or
                                                              (code_review.id IS NOT NULL and c.code_review_id = code_review.id))
            where pr.id = :rewardId
            order by start_date desc, end_date desc offset :offset limit :limit""";

    private final EntityManager entityManager;

    public RewardViewEntity findProjectRewardViewEntityByd(final UUID rewardId) {
        try {
            return (RewardViewEntity) entityManager.createNativeQuery(FIND_PROJECT_REWARD_BY_ID, RewardViewEntity.class)
                    .setParameter("rewardId", rewardId)
                    .getSingleResult();
        } catch (NoResultException noResultException) {
            throw OnlyDustException.notFound("Reward not found", noResultException);
        }
    }

    public RewardViewEntity findUserRewardViewEntityByd(final UUID rewardId) {
        try {
            return (RewardViewEntity) entityManager.createNativeQuery(FIND_USER_REWARD_BY_ID, RewardViewEntity.class)
                    .setParameter("rewardId", rewardId)
                    .getSingleResult();
        } catch (NoResultException noResultException) {
            throw OnlyDustException.notFound("Reward not found", noResultException);
        }
    }

    public Integer countRewardItemsForRewardId(UUID rewardId) {
        final var query = entityManager
                .createNativeQuery(COUNT_REWARD_ITEMS)
                .setParameter("rewardId", rewardId);
        return ((Number) query.getSingleResult()).intValue();
    }

    public List<RewardItemViewEntity> findRewardItemsByRewardId(UUID rewardId, int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(FIND_REWARD_ITEMS, RewardItemViewEntity.class)
                .setParameter("rewardId", rewardId)
                .setParameter("offset", PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex))
                .setParameter("limit", PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex))
                .getResultList()
                ;
    }
}
