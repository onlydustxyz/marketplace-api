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
            select p.*
            from public_projects p
            order by p.rank desc nulls last,
                     p.name
            """, countQuery = "select count(*) from public_projects", nativeQuery = true)
    Page<PublicProjectReadEntity> findAllRecommendedForUser(Long githubUserId, Pageable pageable);

    @Query(value = """
            select p.*
            from foo.p_user_project_recommendations pr
                    join projects p on p.id = pr.project_id
            where pr.github_user_id = :githubUserId
            order by pr.rank,
                     p.name
            """, nativeQuery = true)
    Page<PublicProjectReadEntity> findTopRecommendedForUser(Long githubUserId, Pageable pageable);

    @Query(value = """
            select p
            from PublicProjectReadEntity p where p.id = :id""")
    Optional<PublicProjectReadEntity> findById(UUID id);
}
