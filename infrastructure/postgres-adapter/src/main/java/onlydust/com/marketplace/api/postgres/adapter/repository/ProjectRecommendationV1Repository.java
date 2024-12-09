package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.ProjectRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRecommendationV1Repository extends JpaRepository<ProjectRecommendationEntity, UUID> {

    @Query(value = """
            SELECT project_id   as project_id,
                   0            as score
            FROM (SELECT DISTINCT ON (project_id) cd.project_id, cd.timestamp
                  FROM bi.p_contribution_data cd
                  WHERE cd.contribution_type = 'PULL_REQUEST'
                    and cd.contribution_status = 'COMPLETED'
                    and cd.project_id is not null
                  ORDER BY cd.project_id, cd.timestamp DESC) t
            ORDER BY timestamp DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProjectRecommendationEntity> findLastActiveProjects(int limit);

    @Query(value = """
            SELECT p.id     as project_id,
                   p.rank   as score
            FROM projects p
            ORDER BY p.rank DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProjectRecommendationEntity> findTopProjects(int limit);
} 