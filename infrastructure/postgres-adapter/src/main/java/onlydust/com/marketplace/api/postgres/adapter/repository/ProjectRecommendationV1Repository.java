package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.ProjectRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRecommendationV1Repository extends JpaRepository<ProjectRecommendationEntity, UUID> {

    @Query(value = """
            SELECT p.project_id as project_id,
                   0            as score
            FROM (SELECT DISTINCT ON (project_id) cd.project_id, cd.timestamp
                  FROM bi.p_contribution_data cd
                  WHERE cd.contribution_type = 'PULL_REQUEST'
                    and cd.contribution_status = 'COMPLETED'
                    and cd.project_id is not null
                  ORDER BY cd.project_id, cd.timestamp DESC) t
                     JOIN bi.p_project_global_data p ON p.project_id = t.project_id
                     JOIN reco.user_answers_v1 ua ON ua.user_id = :userId and
                                                     (array_length(ua.languages, 1) = 0 or
                                                      ua.languages && cast(ARRAY ['00000000-0000-0000-0000-000000000000'] as uuid[]) or
                                                      ua.languages && p.language_ids) and
                                                     (array_length(ua.ecosystems, 1) = 0 or
                                                      ua.ecosystems && cast(ARRAY ['00000000-0000-0000-0000-000000000000'] as uuid[]) or
                                                      ua.ecosystems && p.ecosystem_ids)
            ORDER BY timestamp DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProjectRecommendationEntity> findLastActiveProjects(UUID userId, int limit);

    @Query(value = """
            SELECT p.id   as project_id,
                   p.rank as score
            FROM projects p
                     JOIN bi.p_project_global_data pgd ON pgd.project_id = p.id
                     JOIN reco.user_answers_v1 ua ON ua.user_id = :userId and
                                                     (array_length(ua.languages, 1) = 0 or
                                                      ua.languages && cast(ARRAY ['00000000-0000-0000-0000-000000000000'] as uuid[]) or
                                                      ua.languages && pgd.language_ids) and
                                                     (array_length(ua.ecosystems, 1) = 0 or
                                                      ua.ecosystems && cast(ARRAY ['00000000-0000-0000-0000-000000000000'] as uuid[]) or
                                                      ua.ecosystems && pgd.ecosystem_ids)
            ORDER BY p.rank DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProjectRecommendationEntity> findTopProjects(UUID userId, int limit);

    @Query(value = """
            select d.project_id,
                   d.language_score + d.ecosystem_score + d.experience_score + d.maturity_score + d.community_score as score
            from (select p.project_id,
                         p.project_name,
                         (case
                              when ua.languages is null or array_length(ua.languages, 1) = 0 or ua.languages && cast(ARRAY ['00000000-0000-0000-0000-000000000000'] as uuid[]) then 0
                              when ua.learning_preference = 1 then 100.0
                              when ua.learning_preference = 2 then 10.0
                              else 0 end) * (clamp(uap.common_languages_count, 0, 1)) as language_score,
                         100.0 * (case
                                      when ua.ecosystems is null or array_length(ua.ecosystems, 1) = 0 or ua.ecosystems && cast(ARRAY ['00000000-0000-0000-0000-000000000000'] as uuid[]) then 0
                                      else uap.common_ecosystems_count_normalized end)         as ecosystem_score,
                         5.0 * (case
                                    when ua.learning_preference = 1 then 1 - abs(uap.experience_level_diff) / 3.0
                                    when ua.learning_preference = 2 then 1 - abs(uap.experience_level_diff + 1) / 3.0
                                    else 0 end)                                                as experience_score,
                         5.0 * (case
                                    when ua.project_maturity = 1 then pe.well_established_score_normalized
                                    when ua.project_maturity = 2 then pe.emerging_score_normalized
                                    else 0 end)                                                as maturity_score,
                         (case
                              when ua.community_importance = 1 then 3.0
                              when ua.community_importance = 2 then 6.0
                              else 0 end) * pe.active_community_score_normalized               as community_score
                  from bi.p_project_global_data p
                           join reco.m_projects_computed_data pc on p.project_id = pc.project_id
                           join reco.m_projects_extrapolated_data pe on p.project_id = pe.project_id
                           join reco.user_answers_v1 ua on ua.user_id = :userId
                           join reco.user_answers_to_projects_data uap on uap.project_id = p.project_id and uap.user_id = ua.user_id) d
            order by score desc
            LIMIT :limit
            """, nativeQuery = true)
    List<ProjectRecommendationEntity> findTopMatchingProjects(UUID userId, int limit);

    @Modifying
    @Query(nativeQuery = true, value = """
            REFRESH MATERIALIZED VIEW CONCURRENTLY reco.m_projects_computed_data;
            REFRESH MATERIALIZED VIEW CONCURRENTLY reco.m_projects_extrapolated_data;
            """)
    void refreshMaterializedViews();
} 