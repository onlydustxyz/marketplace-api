package onlydust.com.marketplace.api.postgres.adapter.repository;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.view.ProjectContributorsLinkView;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class CustomContributorRepository {

    protected static final String FIND_TOP_CONTRIBUTORS_BASE_QUERY = """
            SELECT
            		ga.id AS github_user_id,
            		ga.login,
            		user_avatar_url(ga.id, ga.avatar_url) as avatar_url,
            		ga.html_url,
                    u.github_user_id IS NOT NULL as is_registered
            FROM
                indexer_exp.github_accounts ga
                JOIN projects_contributors pc ON pc.github_user_id = ga.id
                    AND pc.project_id = :projectId
                LEFT JOIN iam.users u ON u.github_user_id = ga.id
                ORDER BY
                    pc.completed_contribution_count DESC, ga.id
                LIMIT :limit
            """;

    protected static final String GET_CONTRIBUTOR_COUNT = """
                select count(*)
                from projects_contributors pc
                join indexer_exp.github_accounts ga on ga.id = pc.github_user_id
                where pc.project_id = :projectId
                  and ga.login ilike '%' || :login || '%'
            """;
    @Language("PostgreSQL")
    protected static final String GET_CONTRIBUTORS_FOR_PROJECT = """
            select ga.id                                               as id,
                   ga.login                                            as login,
                   user_avatar_url(ga.id, ga.avatar_url)               as avatar_url,
                   pc.completed_contribution_count                     as contribution_count,
                   u.github_user_id is not null                        as is_registered,
                   (select count(distinct r.id)
                    from rewards r
                    where r.project_id = :projectId
                      and r.recipient_id = ga.id)                      as reward_count,
                   coalesce(to_rewards_stats.total_count, 0)           as to_reward_count,
                   to_rewards_stats.pull_request_count                 as prs_to_reward,
                   to_rewards_stats.issue_count                        as issues_to_reward,
                   to_rewards_stats.code_review_count                  as code_reviews_to_reward,
                        
                   coalesce(totals_earned.total_dollars_equivalent, 0) as earned,
                   totals_earned.totals_earned_per_currency            as totals_earned,
                        
                   hc.contributor_github_user_id is not null           as is_hidden
                        
            from projects_contributors pc
                     join indexer_exp.github_accounts ga on ga.id = pc.github_user_id
                     left join iam.users u on u.github_user_id = ga.id
                        
                     left join hidden_contributors hc
                               on hc.contributor_github_user_id = pc.github_user_id and hc.project_id = :projectId and
                                  hc.project_lead_id = CAST(CAST(:projectLeadId AS TEXT) AS UUID)
                        
                     left join (select count(distinct c.id)                                          as total_count,
                                       count(distinct c.id) filter ( where c.type = 'PULL_REQUEST' ) as pull_request_count,
                                       count(distinct c.id) filter ( where c.type = 'CODE_REVIEW' )  as code_review_count,
                                       count(distinct c.id) filter ( where c.type = 'ISSUE' )        as issue_count,
                                       pgr.project_id                                                as project_id,
                                       c.contributor_id                                              as contributor_id
                                from project_github_repos pgr
                                         join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                                         join indexer_exp.contributions c on c.repo_id = gr.id
                                         left join reward_items ri on ri.id = coalesce(cast(c.pull_request_id as text),
                                                                                       cast(c.issue_id as text),
                                                                                       c.code_review_id) and
                                                                      ri.recipient_id = c.contributor_id
                                         left join ignored_contributions ic
                                                   on ic.contribution_id = c.id and ic.project_id = :projectId
                                where gr.visibility = 'PUBLIC'
                                  and c.status = 'COMPLETED'
                                  and ri.id is null
                                  and ic.project_id is null
                                group by pgr.project_id, c.contributor_id) to_rewards_stats
                               on to_rewards_stats.contributor_id = ga.id and to_rewards_stats.project_id = pc.project_id
                        
                     left join (select user_rewards.recipient_id                  as recipient_id,
                                       user_rewards.project_id                    as project_id,
                                       sum(user_rewards.total_dollars_equivalent) as total_dollars_equivalent,
                                       jsonb_agg(jsonb_build_object(
                                               'total_amount', user_rewards.total_amount,
                                               'total_dollars_equivalent', user_rewards.total_dollars_equivalent,
                                               'currency_id', user_rewards.currency_id,
                                               'currency_code', user_rewards.currency_code,
                                               'currency_name', user_rewards.currency_name,
                                               'currency_decimals', user_rewards.currency_decimals,
                                               'currency_logo_url', user_rewards.currency_logo_url
                                                 ))                               as totals_earned_per_currency
                                from (select r.project_id                                as project_id,
                                             r.recipient_id                              as recipient_id,
                                             sum(r.amount)                               as total_amount,
                                             coalesce(sum(rsd.amount_usd_equivalent), 0) as total_dollars_equivalent,
                                             c.id                                        as currency_id,
                                             c.code                                      as currency_code,
                                             c.name                                      as currency_name,
                                             c.decimals                                  as currency_decimals,
                                             c.logo_url                                  as currency_logo_url
                                      from rewards r
                                               join accounting.reward_status_data rsd on rsd.reward_id = r.id
                                               join accounting.reward_statuses rs on rs.reward_id = r.id
                                               join currencies c on c.id = r.currency_id
                                      group by r.recipient_id, c.id, r.project_id) as user_rewards
                                group by user_rewards.recipient_id, user_rewards.project_id) totals_earned
                         on totals_earned.recipient_id = ga.id and totals_earned.project_id = pc.project_id
                        
            where pc.project_id = :projectId
              and ga.login ilike '%' || :login || '%'
              and (hc.contributor_github_user_id is null or :showHidden)
            order by %order_by%
            offset :offset limit :limit
            """;


    protected static final String FIND_REPOS_CONTRIBUTORS = """
            SELECT
                ga.id as github_user_id,
                ga.login,
                user_avatar_url(ga.id, ga.avatar_url) as avatar_url,
                ga.html_url,
                u.github_user_id IS NOT NULL as is_registered
            FROM indexer_exp.github_accounts ga
                LEFT JOIN iam.users u on u.github_user_id = ga.id
            WHERE
                EXISTS(select 1
                       from indexer_exp.repos_contributors rc
                       join indexer_exp.github_repos gr on gr.id = rc.repo_id and gr.visibility = 'PUBLIC'
                       where rc.repo_id in :reposIds and rc.contributor_id = ga.id)
                AND ga.login ilike '%' || :login ||'%'
            ORDER BY ga.login
            LIMIT :limit
            """;

    protected static final String FIND_ALL_CONTRIBUTORS = """
            SELECT
                ga.id as github_user_id,
                ga.login,
                user_avatar_url(ga.id, ga.avatar_url) as avatar_url,
                ga.html_url,
                u.github_user_id IS NOT NULL as is_registered
            FROM indexer_exp.github_accounts ga
                LEFT JOIN iam.users u on u.github_user_id = ga.id
            WHERE
                ga.login ilike '%' || :login ||'%'
            ORDER BY ga.login
            LIMIT :limit
            """;

    protected static final String GET_CONTRIBUTION_CONTRIBUTOR_ID = """
            SELECT contributor_id
            FROM indexer_exp.contributions
            WHERE id = :contributionId
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

    public List<ContributorViewEntity> findProjectTopContributors(UUID projectId, int limit) {
        return entityManager
                .createNativeQuery(FIND_TOP_CONTRIBUTORS_BASE_QUERY, ContributorViewEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("limit", limit)
                .getResultList();
    }

    public Integer getProjectContributorCount(UUID projectId, String login) {
        final var query = entityManager
                .createNativeQuery(GET_CONTRIBUTOR_COUNT)
                .setParameter("projectId", projectId)
                .setParameter("login", login != null ? login : "");
        return ((Number) query.getSingleResult()).intValue();
    }

    public List<ProjectContributorViewEntity> getProjectContributorViewEntity(final UUID projectId, String login, final UUID projectLeadId, Boolean showHidden,
                                                                              ProjectContributorsLinkView.SortBy sortBy,
                                                                              SortDirection sortDirection,
                                                                              int pageIndex, int pageSize) {
        return entityManager.createNativeQuery(buildQuery(sortBy, sortDirection),
                        ProjectContributorViewEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("projectLeadId", projectLeadId)
                .setParameter("showHidden", showHidden)
                .setParameter("login", login != null ? login : "")
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

    public List<ContributorViewEntity> findAllContributorsByLogin(String login, int limit) {
        return entityManager
                .createNativeQuery(FIND_ALL_CONTRIBUTORS, ContributorViewEntity.class)
                .setParameter("login", login != null ? login : "")
                .setParameter("limit", limit)
                .getResultList();
    }

    public Optional<Long> getContributionContributorId(String contributionId) {
        final var result = entityManager
                .createNativeQuery(GET_CONTRIBUTION_CONTRIBUTOR_ID)
                .setParameter("contributionId", contributionId)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of((Long) result.get(0));
    }
}
