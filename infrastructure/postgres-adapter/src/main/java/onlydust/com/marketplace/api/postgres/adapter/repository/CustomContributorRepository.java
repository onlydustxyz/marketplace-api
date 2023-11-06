package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
            		WHERE
            			c.user_id = gu.id
            			AND c.repo_id = ANY ((select array(select pgr.github_repo_id from project_github_repos pgr where pgr.project_id = :projectId))\\:\\:bigint[])
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
                   (select count(distinct pr.id)
                    from payment_requests pr
                    where pr.project_id = :projectId
                      and pr.recipient_id = gu.id)               reward_count,
                   coalesce(to_rewards_stats.total_count,0) to_reward_count,
                   to_rewards_stats.pull_request_count prs_to_reward,
                   to_rewards_stats.issue_count issues_to_reward,
                   to_rewards_stats.code_review_count code_reviews_to_reward,
                   amounts.usd,
                          amounts.eth,
                          coalesce(amounts.eth * cuq_eth.price, 0)   eth_usd,
                          amounts.stark,
                          coalesce(amounts.stark * cuq_stark.price, 0) stark_usd,
                          amounts.apt,
                          coalesce(amounts.apt * cuq_apt.price, 0)   apt_usd,
                          amounts.op,
                          coalesce(amounts.op * cuq_op.price, 0)    op_usd,
                          coalesce(amounts.eth * cuq_eth.price, 0) + coalesce(amounts.stark * cuq_stark.price, 0) +
                          coalesce(amounts.apt * cuq_apt.price, 0) + coalesce(amounts.op * cuq_op.price, 0) +
                          coalesce(amounts.usd, 0)                   earned
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
                                         left join work_items wi on wi.id = c.details_id and wi.recipient_id = c.user_id
                                         left join ignored_contributions ic on ic.contribution_id = c.id
                                where pgr.project_id = :projectId
                                  and c.status = 'complete'
                                  and wi.id is null
                                  and ic.project_id is null
                                group by c.user_id) to_rewards_stats on to_rewards_stats.user_id = gu.id
                     left join (select sum(pr.amount) filter (where pr.currency = 'usd')   usd,
                                                sum(pr.amount) filter (where pr.currency = 'apt')   apt,
                                                sum(pr.amount) filter (where pr.currency = 'stark') stark,
                                                sum(pr.amount) filter (where pr.currency = 'op')    op,
                                                sum(pr.amount) filter (where pr.currency = 'eth')   eth,
                                                pr.recipient_id
                                         from payment_requests pr
                                         where pr.project_id = :projectId
                                         group by pr.recipient_id) amounts on amounts.recipient_id = gu.id
                              left join crypto_usd_quotes cuq_eth on cuq_eth.currency = 'eth'
                              left join crypto_usd_quotes cuq_apt on cuq_apt.currency = 'apt'
                              left join crypto_usd_quotes cuq_stark on cuq_stark.currency = 'stark'
                              left join crypto_usd_quotes cuq_op on cuq_op.currency = 'op'
            where pc.project_id = :projectId
            order by %order_by%
            offset :offset limit :limit
            """;


    protected static final String FIND_REPOS_CONTRIBUTORS = """
            WITH users AS (
                SELECT github_user_id
                FROM auth_users
                UNION
                SELECT github_user_id
                FROM iam.users
            )
            SELECT
                gu.id as github_user_id,
                gu.login,
                gu.avatar_url,
                u.github_user_id IS NOT NULL as is_registered
            FROM github_users gu
                LEFT JOIN users u on u.github_user_id = gu.id
            WHERE
                EXISTS(select 1 from contributions c where c.repo_id in :reposIds and c.user_id = gu.id)
                AND gu.login ilike '%' || :login ||'%'
            ORDER BY gu.login
            LIMIT :limit
            """;


    private final EntityManager entityManager;

    static protected String buildQuery(ProjectContributorsLinkView.SortBy sortBy, SortDirection sortDirection) {
        final String direction = Optional.ofNullable(sortDirection).map(SortDirection::name).orElse("asc");
        final String sortValue = Optional.ofNullable(sortBy).map(sort -> switch (sortBy) {
            case login -> "login " + direction;
            case earned -> "earned " + direction + ", login asc";
            case contributionCount -> "contribution_count " + direction + ", login asc";
            case rewardCount -> "reward_count " + direction + ", login asc";
            case toRewardCount -> "to_reward_count " + direction + ", login asc";
        }).orElse("login " + direction);
        return GET_CONTRIBUTORS_FOR_PROJECT
                .replace("%order_by%", sortValue);
    }

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

    public List<ProjectContributorViewEntity> getProjectContributorViewEntity(final UUID projectId,
                                                                              ProjectContributorsLinkView.SortBy sortBy,
                                                                              SortDirection sortDirection,
                                                                              int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(buildQuery(sortBy, sortDirection),
                        ProjectContributorViewEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("offset", PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex))
                .setParameter("limit", PaginationMapper.getPostgresLimitFromPagination(pageSize, pageIndex))
                .getResultList();
    }

    public List<ContributorViewEntity> findReposContributorsByLogin(Set<Long> reposIds, String login, int limit) {
        return entityManager
                .createNativeQuery(FIND_REPOS_CONTRIBUTORS, ContributorViewEntity.class)
                .setParameter("reposIds", reposIds)
                .setParameter("login", login != null ? login : "")
                .setParameter("limit", limit)
                .getResultList();
    }
}
