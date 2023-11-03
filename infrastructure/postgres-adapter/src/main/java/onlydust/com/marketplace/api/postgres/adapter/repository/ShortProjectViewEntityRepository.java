package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortProjectViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ShortProjectViewEntityRepository extends JpaRepository<ShortProjectViewEntity, UUID> {
    @Query(value = """
            SELECT
                p.project_id as id,
                p.key,
                p.name,
                p.short_description,
                p.long_description,
                p.logo_url,
                p.telegram_link,
                p.hiring,
                p.visibility
            FROM 
                 project_details p
            WHERE
                EXISTS(
                    SELECT 1 
                    FROM 
                        project_github_repos pgr
                    INNER JOIN indexer_exp.contributions c on c.repo_id = pgr.github_repo_id 
                    WHERE 
                        p.project_id = pgr.project_id AND
                        contributor_id = :contributorId AND 
                        (COALESCE(:repoIds) IS NULL OR pgr.github_repo_id IN :repoIds)
                )
                AND (COALESCE(:projectIds) IS NULL OR p.project_id IN :projectIds)
            """, nativeQuery = true)
    List<ShortProjectViewEntity> listProjectsByContributor(Long contributorId,
                                                           List<UUID> projectIds,
                                                           List<Long> repoIds);
}
