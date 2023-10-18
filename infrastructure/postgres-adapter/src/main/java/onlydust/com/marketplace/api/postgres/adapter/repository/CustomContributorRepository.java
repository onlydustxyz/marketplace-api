package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubUserViewEntity;

import javax.persistence.EntityManager;
import java.util.List;
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
}
