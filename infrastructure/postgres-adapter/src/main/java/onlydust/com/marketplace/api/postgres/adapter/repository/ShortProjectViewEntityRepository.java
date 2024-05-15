package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortProjectQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ShortProjectViewEntityRepository extends JpaRepository<ShortProjectQueryEntity, UUID> {
    @Query(value = """
            SELECT
                p.id,
                p.slug,
                p.name,
                p.short_description,
                p.long_description,
                p.logo_url,
                p.telegram_link,
                p.hiring,
                p.visibility
            FROM 
                 projects p
            WHERE
                EXISTS(
                    SELECT 1 
                    FROM 
                        project_github_repos pgr
                    JOIN indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                    INNER JOIN indexer_exp.contributions c on c.repo_id = gr.id 
                    WHERE 
                        p.id = pgr.project_id AND
                        gr.visibility = 'PUBLIC' AND
                        contributor_id = :rewardId AND 
                        (COALESCE(:repoIds) IS NULL OR pgr.github_repo_id IN (:repoIds))
                )
                AND (COALESCE(:projectIds) IS NULL OR p.id IN (:projectIds))
            ORDER BY 
                p.name 
            """, nativeQuery = true)
    List<ShortProjectQueryEntity> listProjectsByContributor(Long rewardId,
                                                            List<UUID> projectIds,
                                                            List<Long> repoIds);

    @Query(value = """
            SELECT DISTINCT
                p.id,
                p.slug,
                p.name,
                p.short_description,
                p.long_description,
                p.logo_url,
                p.telegram_link,
                p.hiring,
                p.visibility
            FROM 
                 projects p
            JOIN rewards r ON r.project_id = p.id
            WHERE
                r.recipient_id = :rewardId
            ORDER BY 
                p.name 
            """, nativeQuery = true)
    List<ShortProjectQueryEntity> listProjectsByRewardRecipient(Long rewardId);
}
