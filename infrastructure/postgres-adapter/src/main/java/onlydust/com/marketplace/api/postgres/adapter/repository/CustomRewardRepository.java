package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardItemViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class CustomRewardRepository {
    private static final String FIND_PROJECT_REWARD_BY_ID = """
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
                gu_recipient.login                                                          recipient_login,
                user_avatar_url(gu_recipient.id, gu_recipient.avatar_url)                   recipient_avatar_url,
                gu_recipient.id                                                             recipient_id,
                gu_requestor.login                                                          requestor_login,
                user_avatar_url(gu_requestor.id, gu_requestor.avatar_url)                   requestor_avatar_url,
                gu_requestor.id                                                             requestor_id,
                pr.id,
                pr.amount,
                pr.currency,
                pr.invoice_id,
                (select count(id) from work_items wi where wi.payment_id = pr.id)           contribution_count,
                pr.usd_amount                                                               dollars_equivalent,
                case
                when r.id is not null then 'COMPLETE'
                when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') THEN 'LOCKED'
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
                else 'PROCESSING'
                end                                                                     status,
                r.receipt,
                p.project_id,
                p.key               as                                                      project_key,
                p.name              as                                                      project_name,
                p.short_description as                                                      project_short_description,
                p.long_description  as                                                      project_long_description,
                p.logo_url          as                                                      project_logo_url,
                p.telegram_link     as                                                      project_telegram_link,
                p.hiring            as                                                      project_hiring,
                p.visibility        as                                                      project_visibility
                from payment_requests pr
                join project_details p on p.project_id = pr.project_id
                left join indexer_exp.github_accounts gu_recipient on gu_recipient.id = pr.recipient_id
                left join iam.users u on pr.recipient_id = u.github_user_id
                left join iam.users u_requestor on u_requestor.id = pr.requestor_id
                left join indexer_exp.github_accounts gu_requestor on gu_requestor.id = u_requestor.github_user_id
                left join payments r on r.request_id = pr.id
                left join billing_profile_check bpc on bpc.user_id = u.id
                LEFT JOIN payout_checks ON payout_checks.github_user_id = pr.recipient_id
                where pr.id = :rewardId""";
    private static final String FIND_USER_REWARD_BY_ID = """
            with billing_profile_check as (select ubpt.user_id,
                                       ubpt.billing_profile_type type,
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
                       gu_recipient.login                                                          recipient_login,
                       user_avatar_url(gu_recipient.id, gu_recipient.avatar_url)                   recipient_avatar_url,
                       gu_recipient.id                                                             recipient_id,
                       gu_requestor.login                                                          requestor_login,
                       user_avatar_url(gu_requestor.id, gu_requestor.avatar_url)                   requestor_avatar_url,
                       gu_requestor.id                                                             requestor_id,
                       pr.id,
                       pr.invoice_id,
                       pr.amount,
                       pr.currency,
                       (select count(id) from work_items wi where wi.payment_id = pr.id)           contribution_count,
                       pr.usd_amount                                                               dollars_equivalent,
                       case
                           when r.id is not null then 'COMPLETE'
                           when not coalesce(bpc.billing_profile_verified, false) then 'PENDING_VERIFICATION'
                           when (case
                                     when pr.currency in ('eth', 'lords', 'usdc')
                                         then not payout_checks.wallets @> array [cast('ethereum' as network)]
                                     when pr.currency = 'strk' then not payout_checks.wallets @> array [cast('starknet' as network)]
                                     when pr.currency = 'op' then not payout_checks.wallets @> array [cast('optimism' as network)]
                                     when pr.currency = 'apt' then not payout_checks.wallets @> array [cast('aptos' as network)]
                                     when pr.currency = 'usd' then not payout_checks.has_bank_account
                               end) then 'MISSING_PAYOUT_INFO'
                           when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') THEN 'LOCKED'
                           when coalesce(pr.invoice_received_at, i.created_at) is null then 'PENDING_INVOICE'
                           else 'PROCESSING'
                           end                                                                     status,
                       r.receipt,
                       p.project_id,
                       p.key               as                                                      project_key,
                       p.name              as                                                      project_name,
                       p.short_description as                                                      project_short_description,
                       p.long_description  as                                                      project_long_description,
                       p.logo_url          as                                                      project_logo_url,
                       p.telegram_link     as                                                      project_telegram_link,
                       p.hiring            as                                                      project_hiring,
                       p.visibility        as                                                      project_visibility
                from payment_requests pr
                         join project_details p on p.project_id = pr.project_id
                         left join indexer_exp.github_accounts gu_recipient on gu_recipient.id = pr.recipient_id
                         left join iam.users u on pr.recipient_id = u.github_user_id
                         left join iam.users u_requestor on u_requestor.id = pr.requestor_id
                         left join indexer_exp.github_accounts gu_requestor on gu_requestor.id = u_requestor.github_user_id
                         left join payments r on r.request_id = pr.id
                         LEFT JOIN payout_checks ON payout_checks.github_user_id = pr.recipient_id
                         LEFT JOIN billing_profile_check bpc on bpc.user_id = u.id
                         LEFT JOIN accounting.invoices i on i.id = pr.invoice_id and i.status in ('TO_REVIEW', 'APPROVED', 'PAID')
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
                                               gpr.commit_count                             commits_count,
                                               gpr.body                                     github_body
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
                                              gi.comments_count,
                                              gi.body                                     github_body
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
                                              gprr.author_html_url       author_github_url,
                                              gprr.body                  github_body
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
                            coalesce(pull_request.github_body, issue.github_body, code_review.github_body) github_body,
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
