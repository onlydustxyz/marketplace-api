package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface PublicProjectReadRepository extends Repository<PublicProjectReadEntity, UUID> {
    @Query(value = """
            with recommended_projects as (select ps.project2_id    as project_id,
                                                 pc.github_user_id as github_user_id,
                                                 sum(ps.rank)      as rank
                                          from projects_contributors pc
                                                   join project_similarities ps on ps.project1_id = pc.project_id
                                          group by ps.project2_id, pc.github_user_id)
            select p.*
            from public_projects p
                     left join recommended_projects rc on rc.project_id = p.id and rc.github_user_id = :githubUserId
                     left join projects_contributors pc on pc.project_id = rc.project_id and pc.github_user_id = rc.github_user_id
            where pc.project_id is null
            order by rc.rank desc nulls last,
                     p.rank desc nulls last,
                     p.name
            """, countQuery = "select count(*) from projects", nativeQuery = true)
    Page<PublicProjectReadEntity> findAllRecommendedForUser(Long githubUserId, Pageable pageable);

    @Query(value = """
            select p
            from PublicProjectReadEntity p where p.id = :id""")
    Optional<PublicProjectReadEntity> findById(UUID id);
}
