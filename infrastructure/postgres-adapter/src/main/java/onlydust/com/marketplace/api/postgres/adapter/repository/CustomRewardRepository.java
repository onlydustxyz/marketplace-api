package onlydust.com.marketplace.api.postgres.adapter.repository;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardItemViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class CustomRewardRepository {
    private static final String COUNT_REWARD_ITEMS = """
            select count(distinct ri.id)
            from rewards r
                     join reward_items ri on ri.reward_id = r.id
            where r.id = :rewardId""";
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
            select distinct ri.type,
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
                            coalesce(pull_request.author_github_url, issue.author_github_url, code_review.author_github_url) author_github_url,
                            coalesce(pull_request.github_body, issue.github_body, code_review.github_body) github_body,
                            pull_request.commits_count,
                            (select gprcc.commit_count
                             from indexer_exp.github_pull_request_commit_counts gprcc
                             where gprcc.pull_request_id = pull_request.id and gprcc.author_id = r.recipient_id) user_commits_count,
                            issue.comments_count,
                            r.recipient_id,
                            r.billing_profile_id
            from rewards r
                     join reward_items ri on ri.reward_id = r.id
                     left join get_issue issue on issue.id = (case when ri.id ~ '^[0-9]+$' then cast(ri.id as bigint) else -1 end)
                     left join get_pr pull_request on pull_request.id = (case when ri.id ~ '^[0-9]+$' then cast(ri.id as bigint) else -1 end)
                     left join get_code_review code_review on code_review.id = ri.id
                     left join indexer_exp.contributions c on c.contributor_id = r.recipient_id and c.repo_id = ri.repo_id and
                                                              -- check that the repo is still part of the project (if it's not, then the contribution doesn't belong to the project)
                                                              exists (select 1 from project_github_repos pgr where pgr.github_repo_id = c.repo_id and pgr.project_id = r.project_id) and
                                                              ((issue.id IS NOT NULL and c.issue_id = issue.id) or
                                                              (pull_request.id IS NOT NULL and c.pull_request_id = pull_request.id) or
                                                              (code_review.id IS NOT NULL and c.code_review_id = code_review.id))
            where r.id = :rewardId
            order by start_date desc, end_date desc offset :offset limit :limit""";

    private final EntityManager entityManager;

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
