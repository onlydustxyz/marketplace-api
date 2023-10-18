package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class CustomContributorRepository {

    protected static final String FIND_TOP_CONTRIBUTORS_BASE_QUERY = """
            SELECT
            	(
            		SELECT
            			count(*)
            		FROM
            			contributions c
            			JOIN project_github_repos pgr ON pgr.project_id = :projectId
            				AND pgr.github_repo_id = c.repo_id
            		WHERE
            			c.user_id = gu.id
            			AND c.status = 'complete') AS contribution_count,
            		gu.*
            	FROM
            		github_users gu
            		JOIN projects_contributors pc ON pc.github_user_id = gu.id
            			AND pc.project_id = :projectId
            		ORDER BY
            			contribution_count DESC
            		LIMIT :limit
            """;

    protected static final String GET_CONTRIBUTOR_COUNT = """
                select count(*)
                from projects_contributors pc
                where pc.project_id = :projectId
            """;

    private final EntityManager entityManager;

    public List<GithubUserViewEntity> findProjectTopContributors(UUID projectId, int limit) {
        return entityManager
                .createNativeQuery(FIND_TOP_CONTRIBUTORS_BASE_QUERY, GithubUserViewEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("limit", limit)
                .getResultList();
    }

    public Integer getProjectContributorCount(UUID projectId) {
        final var query = entityManager
                .createNativeQuery(GET_CONTRIBUTOR_COUNT)
                .setParameter("projectId", projectId);
        return ((Number) query.getSingleResult()).intValue();
    }

    protected static final String GET_CONTRIBUTORS_FOR_PROJECT = """
            select gu.id,
                   gu.login,
                   gu.avatar_url,
                   (select count(distinct c.id)
                    from project_github_repos pgr
                             left join contributions c on c.repo_id = pgr.github_repo_id
                    where pgr.project_id = :projectId
                      and user_id = gu.id
                      and c.status = 'complete')                 contribution_count,
                   au.id is not null is_registered,
                   (select sum(pr.amount)
                    from payment_requests pr
                    where pr.project_id = :projectId
                      and pr.recipient_id = gu.id)               earned,
                   (select count(distinct pr.id)
                    from payment_requests pr
                    where pr.project_id = :projectId
                      and pr.recipient_id = gu.id)               reward_count,
                   to_rewards_stats.total_count to_reward_count,
                   to_rewards_stats.pull_request_count prs_to_reward,
                   to_rewards_stats.issue_count issues_to_reward,
                   to_rewards_stats.code_review_count code_reviews_to_reward
            from projects_contributors pc
                     join github_users gu on gu.id = pc.github_user_id
                     left join auth_users au on au.github_user_id = gu.id
                     left join (select count(distinct c.id)                                          total_count,
                                       count(distinct c.id) filter ( where c.type = 'pull_request' ) pull_request_count,
                                       count(distinct c.id) filter ( where c.type = 'code_review' )  code_review_count,
                                       count(distinct c.id) filter ( where c.type = 'issue' )        issue_count,
                                       c.user_id
                                from project_github_repos pgr
                                         left join contributions c on c.repo_id = pgr.github_repo_id
                                         left join work_items wi on wi.id = c.details_id
                                where pgr.project_id = :projectId
                                  and c.status = 'complete'
                                  and wi.id is null
                                group by c.user_id) to_rewards_stats on to_rewards_stats.user_id = gu.id
            where pc.project_id = :projectId
            order by %order_by%
            offset %offset% limit %limit%
            """;


    public List<ProjectContributorViewEntity> getProjectContributorViewEntity(final UUID projectId,
                                                                              ProjectContributorsLinkView.SortBy sortBy,
                                                                              int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(buildQuery(sortBy, pageIndex, pageSize),
                        ProjectContributorViewEntity.class)
                .setParameter("projectId", projectId)
                .getResultList();
    }

    static protected String buildQuery(ProjectContributorsLinkView.SortBy sortBy,
                                       int pageIndex, int pageSize) {
        final String sortValue = Optional.ofNullable(sortBy).map(sort -> switch (sortBy) {
            case login -> "login";
            case earned -> "earned desc";
            case contributionCount -> "contribution_count desc";
            case rewardCount -> "reward_count desc";
            case toRewardCount -> "to_reward_count desc";
        }).orElse("login");
        return GET_CONTRIBUTORS_FOR_PROJECT
                .replace("%offset%",
                        PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex).toString())
                .replace("%limit%",
                        PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex).toString())
                .replace("%order_by%", sortValue);
    }
}
