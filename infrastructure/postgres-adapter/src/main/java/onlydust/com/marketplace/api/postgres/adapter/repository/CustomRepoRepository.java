package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubRepoViewEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class CustomRepoRepository {

    protected static final String FIND_PROJECT_REPOS_BASE_QUERY = """
                select
                    r.*,
                    r.languages::text as languages
                from github_repos r
                join project_github_repos pr on pr.github_repo_id = r.id and pr.project_id = :projectId
            """;

    private final EntityManager entityManager;

    public List<GithubRepoViewEntity> findProjectRepos(UUID projectId) {
        return entityManager
                .createNativeQuery(FIND_PROJECT_REPOS_BASE_QUERY, GithubRepoViewEntity.class)
                .setParameter("projectId", projectId)
                .getResultList();
    }
}
