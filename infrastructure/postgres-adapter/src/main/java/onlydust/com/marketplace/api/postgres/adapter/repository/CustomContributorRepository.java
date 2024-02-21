package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import org.intellij.lang.annotations.Language;

import javax.persistence.EntityManager;
import java.math.BigInteger;
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
            select ga.id,
                   ga.login,
                   user_avatar_url(ga.id, ga.avatar_url) as avatar_url,
                   pc.completed_contribution_count                 contribution_count,
                   u.github_user_id is not null is_registered,
                   (select count(distinct pr.id)
                    from payment_requests pr
                    where pr.project_id = :projectId
                      and pr.recipient_id = ga.id)               reward_count,
                   coalesce(to_rewards_stats.total_count,0) to_reward_count,
                   to_rewards_stats.pull_request_count prs_to_reward,
                   to_rewards_stats.issue_count issues_to_reward,
                   to_rewards_stats.code_review_count code_reviews_to_reward,
                   amounts.usd,
                   amounts.usdc,
                   coalesce(amounts.usdc * cuq_usdc.price, 0)   usdc_usd,
                   amounts.eth,
                   coalesce(amounts.eth * cuq_eth.price, 0)   eth_usd,
                   amounts.stark,
                   coalesce(amounts.stark * cuq_stark.price, 0) stark_usd,
                   amounts.apt,
                   coalesce(amounts.apt * cuq_apt.price, 0)   apt_usd,
                   amounts.op,
                   coalesce(amounts.op * cuq_op.price, 0)    op_usd,
                   amounts.lords,
                   coalesce(amounts.lords * cuq_lords.price, 0)    lords_usd,
                   coalesce(amounts.eth * cuq_eth.price, 0) + coalesce(amounts.stark * cuq_stark.price, 0) +
                   coalesce(amounts.apt * cuq_apt.price, 0) + coalesce(amounts.op * cuq_op.price, 0) +
                   coalesce(amounts.lords * cuq_lords.price, 0) + coalesce(amounts.usdc * cuq_usdc.price, 0) +
                   coalesce(amounts.usd, 0)                   earned,
                   hc.contributor_github_user_id is not null is_hidden
            from projects_contributors pc
                     join indexer_exp.github_accounts ga on ga.id = pc.github_user_id
                     left join iam.users u on u.github_user_id = ga.id
                     left join hidden_contributors hc on 
                            hc.contributor_github_user_id = pc.github_user_id and 
                            hc.project_id = :projectId and 
                            hc.project_lead_id = CAST(CAST(:projectLeadId AS TEXT) AS UUID)
                     left join (select count(distinct c.id)                                          total_count,
                                       count(distinct c.id) filter ( where c.type = 'PULL_REQUEST' ) pull_request_count,
                                       count(distinct c.id) filter ( where c.type = 'CODE_REVIEW' )  code_review_count,
                                       count(distinct c.id) filter ( where c.type = 'ISSUE' )        issue_count,
                                       c.contributor_id
                                from project_github_repos pgr
                                         join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                                         join indexer_exp.contributions c on c.repo_id = gr.id
                                         left join work_items wi on wi.id = coalesce(cast(c.pull_request_id as text), cast(c.issue_id as text), c.code_review_id) and wi.recipient_id = c.contributor_id
                                         left join ignored_contributions ic on ic.contribution_id = c.id and ic.project_id = :projectId
                                where pgr.project_id = :projectId
                                  and gr.visibility = 'PUBLIC'
                                  and c.status = 'COMPLETED'
                                  and wi.id is null
                                  and ic.project_id is null
                                group by c.contributor_id) to_rewards_stats on to_rewards_stats.contributor_id = ga.id
                     left join (select sum(pr.amount) filter (where pr.currency = 'usd')   usd,
                                                sum(pr.amount) filter (where pr.currency = 'usdc')   usdc,
                                                sum(pr.amount) filter (where pr.currency = 'apt')   apt,
                                                sum(pr.amount) filter (where pr.currency = 'strk') stark,
                                                sum(pr.amount) filter (where pr.currency = 'op')    op,
                                                sum(pr.amount) filter (where pr.currency = 'eth')   eth,
                                                sum(pr.amount) filter (where pr.currency = 'lords')   lords,
                                                pr.recipient_id
                                         from payment_requests pr
                                         where pr.project_id = :projectId
                                         group by pr.recipient_id) amounts on amounts.recipient_id = ga.id
                              left join crypto_usd_quotes cuq_eth on cuq_eth.currency = 'eth'
                              left join crypto_usd_quotes cuq_usdc on cuq_usdc.currency = 'usdc'
                              left join crypto_usd_quotes cuq_apt on cuq_apt.currency = 'apt'
                              left join crypto_usd_quotes cuq_stark on cuq_stark.currency = 'strk'
                              left join crypto_usd_quotes cuq_op on cuq_op.currency = 'op'
                              left join crypto_usd_quotes cuq_lords on cuq_lords.currency = 'lords'
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
                EXISTS(select 1 from indexer_exp.repos_contributors rc 
                join indexer_exp.github_repos gr on gr.id = rc.repo_id and gr.visibility = 'PUBLIC'
                where rc.repo_id in :reposIds and rc.contributor_id = ga.id)
                AND ga.login ilike '%' || :login ||'%'
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

    public Optional<Long> getContributionContributorId(String contributionId) {
        final var result = entityManager
                .createNativeQuery(GET_CONTRIBUTION_CONTRIBUTOR_ID)
                .setParameter("contributionId", contributionId)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(((BigInteger) result.get(0)).longValue());
    }
}
