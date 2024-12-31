package onlydust.com.marketplace.api.read.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.read.entities.project.SimilarProjectReadEntity;

public interface SimilarProjectReadRepository extends JpaRepository<SimilarProjectReadEntity, UUID> {
    @Query(value = """
            WITH ranked_similarities AS (SELECT project_id,
                                                project_slug,
                                                similar_project_id,
                                                similar_project,
                                                similar_project_rank,
                                                PERCENT_RANK() OVER (
                                                    PARTITION BY project_id
                                                    ORDER BY ecosystem_matches DESC
                                                    ) as similar_ecosystem_rank,
                                                PERCENT_RANK() OVER (
                                                    PARTITION BY project_id
                                                    ORDER BY language_matches DESC
                                                    ) as similar_language_rank,
                                                PERCENT_RANK() OVER (
                                                    PARTITION BY project_id
                                                    ORDER BY contributor_matches DESC
                                                    ) as similar_contributor_rank
                                        FROM bi.m_project_similarities)
            SELECT similar_project_id as id,
                   similar_project    as project
            FROM ranked_similarities
            WHERE (project_id = :projectId OR project_slug = :projectSlug)
            ORDER BY similar_ecosystem_rank + similar_language_rank + similar_contributor_rank,
                     similar_project_rank DESC
            """, 
            countQuery = """
            SELECT COUNT(*)
            FROM bi.m_project_similarities
            WHERE (project_id = :projectId OR project_slug = :projectSlug)
            """, nativeQuery = true)
    Page<SimilarProjectReadEntity> findAll(UUID projectId, String projectSlug, Pageable pageable);
} 