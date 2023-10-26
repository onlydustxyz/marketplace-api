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
    private final EntityManager entityManager;

    private static final String FIND_REWARD_BY_ID = """
            select pr.requested_at,
                   r.processed_at,
                   gu_recipient.login                                                                       recipient_login,
                   gu_recipient.avatar_url                                                                  recipient_avatar_url,
                   gu_recipient.id                                                                          recipient_id,
                   gu_requestor.login                                                                       requestor_login,
                   gu_requestor.avatar_url                                                                  requestor_avatar_url,
                   gu_requestor.id                                                                          requestor_id,
                   pr.id,
                   pr.amount,
                   pr.currency,
                   (select count(id) from work_items wi where wi.payment_id = pr.id)                        contribution_count,
                   case when pr.currency = 'usd' then pr.amount else cuq.price * pr.amount end dollars_equivalent,
                   case
                       when au.id is null then 'PENDING_SIGNUP'
                       when r.id is not null then 'COMPLETE'
                       else 'PROCESSING'
                       end                                                                                  status
            from payment_requests pr
                     join github_users gu_recipient on gu_recipient.id = pr.recipient_id
                     left join public.auth_users au on gu_recipient.id = au.github_user_id
                     join auth_users au_requestor on au_requestor.id = pr.requestor_id
                     join github_users gu_requestor on gu_requestor.id = au_requestor.github_user_id
                     left join crypto_usd_quotes cuq on cuq.currency = pr.currency
                     left join payments r on r.request_id = pr.id
                     where pr.id = :rewardId""";

    public RewardViewEntity findProjectRewardViewEntityByd(final UUID rewardId) {
        try {
            return (RewardViewEntity) entityManager.createNativeQuery(FIND_REWARD_BY_ID, RewardViewEntity.class)
                    .setParameter("rewardId", rewardId)
                    .getSingleResult();
        } catch (NoResultException noResultException) {
            throw OnlyDustException.notFound("Reward not found", noResultException);
        }
    }

    private static final String COUNT_REWARD_ITEMS = """
            select count(distinct wi.id)
            from payment_requests pr
                     join public.work_items wi on pr.recipient_id = wi.recipient_id
            where pr.id = :rewardId""";

    public Integer countRewardItemsForRewardId(UUID rewardId) {
        final var query = entityManager
                .createNativeQuery(COUNT_REWARD_ITEMS)
                .setParameter("rewardId", rewardId);
        return ((Number) query.getSingleResult()).intValue();
    }

    private static final String FIND_REWARD_ITEMS = """
            with get_pr as (select gpr.number,
                                   gpr.id,
                                   gpr.html_url,
                                   gpr.title,
                                   gr.name                                repo_name,
                                   gpr.created_at                         start_date,
                                   coalesce(gpr.closed_at, gpr.merged_at) end_date,
                                   gu.id                                  author_id,
                                   gu.login                               author_login,
                                   gu.avatar_url                          avatar_url,
                                   gu.html_url                            author_github_url,
                                   (select count(c.pull_request_id)
                                    from github_pull_request_commits c
                                    where c.pull_request_id = gpr.id)     commits_count
                            from github_pull_requests gpr
                                     left join github_users gu on gu.id = gpr.author_id
                                     left join github_repos gr on gr.id = gpr.repo_id),
                 get_issue as (select gi.number,
                                      gi.id,
                                      gi.html_url,
                                      gi.title,
                                      gr.name       repo_name,
                                      gi.created_at start_date,
                                      gi.closed_at  end_date,
                                      gu.id         author_id,
                                      gu.login      author_login,
                                      gu.avatar_url avatar_url,
                                      gu.html_url   author_github_url,
                                      gi.comments_count
                               from github_issues gi
                                        left join github_users gu on gu.id = gi.author_id
                                        left join github_repos gr on gr.id = gi.repo_id),
                 get_code_review as (select gpr.number,
                                            gprr.id,
                                            gpr.html_url,
                                            gpr.title,
                                            gr.name           repo_name,
                                            gpr.created_at    start_date,
                                            gprr.submitted_at end_date,
                                            gu.id             author_id,
                                            gu.login          author_login,
                                            gu.avatar_url     avatar_url,
                                            gu.html_url       author_github_url
                                     from github_pull_request_reviews gprr
                                              left join github_users gu on gu.id = gprr.reviewer_id
                                              left join github_pull_requests gpr on gpr.id = gprr.pull_request_id
                                              left join github_repos gr on gr.id = gpr.repo_id)
            select distinct wi.type,
                            coalesce(cast(pull_request.id as text), cast(issue.id as text), cast(code_review.id as text))             contribution_id,
                            c.status,
                            coalesce(pull_request.number, issue.number, code_review.number)                   number,
                            coalesce(pull_request.html_url, issue.html_url, code_review.html_url)             html_url,
                            coalesce(pull_request.title, issue.title, code_review.title)                      title,
                            coalesce(pull_request.repo_name, issue.repo_name, code_review.repo_name)          repo_name,
                            coalesce(pull_request.start_date, issue.start_date, code_review.start_date)       start_date,
                            coalesce(pull_request.end_date, issue.end_date, code_review.end_date)             end_date,
                            coalesce(pull_request.author_id, issue.author_id, code_review.author_id)          author_id,
                            coalesce(pull_request.author_login, issue.author_login, code_review.author_login) author_login,
                            coalesce(pull_request.avatar_url, issue.avatar_url, code_review.avatar_url)       avatar_url,
                            coalesce(pull_request.html_url, issue.html_url, code_review.html_url)             html_url,
                            coalesce(pull_request.author_github_url, issue.author_github_url, code_review.author_github_url) author_github_url,
                            pull_request.commits_count,
                            (select count(c.pull_request_id)
                             from github_pull_request_commits c
                             where c.pull_request_id = pull_request.id
                               and c.author_id = pr.recipient_id)                                             user_commits_count,
                            issue.comments_count
            from payment_requests pr
                      join public.work_items wi on wi.payment_id = pr.id
                      left join get_issue issue on issue.id = (case when wi.id ~ '^[0-9]+$' then cast(wi.id as bigint) else -1 end)
                      left join get_pr pull_request on pull_request.id = (case when wi.id ~ '^[0-9]+$' then cast(wi.id as bigint) else -1 end)
                      left join get_code_review code_review on code_review.id = wi.id
                      join public.contributions c on c.details_id =
                                                                  coalesce(cast(pull_request.id as text), cast(issue.id as text),
                                                                           cast(code_review.id as text))
            where pr.id = :rewardId
            order by start_date desc, end_date desc offset :offset limit :limit""";

    public List<RewardItemViewEntity> findRewardItemsByRewardId(UUID rewardId, int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(FIND_REWARD_ITEMS,RewardItemViewEntity.class)
                .setParameter("rewardId",rewardId)
                .setParameter("offset", PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex))
                .setParameter("limit", PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex))
                .getResultList()
                ;
    }
}
