package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.PublicUserProfileResponseV2Entity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface PublicUserProfileResponseV2EntityRepository extends Repository<PublicUserProfileResponseV2Entity, UUID> {
    @Query(value = """
            select
                u.github_user_id                    as github_user_id
            from
                iam.all_users u
                left join indexer_exp.contributions c on c.contributor_id = u.github_user_id
                left join project_github_repos pgr on c.repo_id = pgr.github_repo_id
                left join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id and gr.visibility = 'PUBLIC'
                left join projects_ecosystems pe on pe.project_id = pgr.project_id
                left join projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
            where
                u.github_user_id = :githubUserId
            group by
                u.github_user_id
            """, nativeQuery = true)
    PublicUserProfileResponseV2Entity findByGithubUserId(Long githubUserId);
}
