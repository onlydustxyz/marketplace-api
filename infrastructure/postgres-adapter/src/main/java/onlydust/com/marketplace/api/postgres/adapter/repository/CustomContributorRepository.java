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
                select
                    (select count(*) from contributions where project_id = :projectId and github_user_id = gu.id and status = 'complete'::contribution_status) as contribution_count,
                    gu.*
                from github_users gu
                join projects_contributors pc on pc.github_user_id = gu.id and pc.project_id = :projectId
                order by contribution_count desc
                limit :limit
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
