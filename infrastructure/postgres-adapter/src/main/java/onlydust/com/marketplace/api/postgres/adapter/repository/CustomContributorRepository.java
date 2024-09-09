package onlydust.com.marketplace.api.postgres.adapter.repository;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorQueryEntity;

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
                    u.github_user_id IS NOT NULL as is_registered,
                    u.id as user_id
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

    protected static final String FIND_REPOS_CONTRIBUTORS = """
            SELECT
                ga.id as github_user_id,
                ga.login,
                user_avatar_url(ga.id, ga.avatar_url) as avatar_url,
                ga.html_url,
                u.github_user_id IS NOT NULL as is_registered,
                u.id as user_id
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
                u.github_user_id IS NOT NULL as is_registered,
                u.id as user_id
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

    public List<ContributorQueryEntity> findProjectTopContributors(UUID projectId, int limit) {
        return entityManager
                .createNativeQuery(FIND_TOP_CONTRIBUTORS_BASE_QUERY, ContributorQueryEntity.class)
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

    public List<ContributorQueryEntity> findReposContributorsByLogin(Set<Long> reposIds, String login, int limit) {
        return entityManager
                .createNativeQuery(FIND_REPOS_CONTRIBUTORS, ContributorQueryEntity.class)
                .setParameter("reposIds", reposIds)
                .setParameter("login", login != null ? login : "")
                .setParameter("limit", limit)
                .getResultList();
    }

    public List<ContributorQueryEntity> findAllContributorsByLogin(String login, int limit) {
        return entityManager
                .createNativeQuery(FIND_ALL_CONTRIBUTORS, ContributorQueryEntity.class)
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
