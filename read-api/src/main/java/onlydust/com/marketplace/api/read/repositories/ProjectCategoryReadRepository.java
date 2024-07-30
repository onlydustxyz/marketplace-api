package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectCategoryReadRepository extends Repository<ProjectCategoryReadEntity, UUID> {
    Optional<ProjectCategoryReadEntity> findById(UUID Id);

    List<ProjectCategoryReadEntity> findAll(Sort sort);

    @Query("""
            SELECT DISTINCT pc
            FROM ProjectCategoryReadEntity pc
            JOIN pc.ecosystems e
            WHERE e.slug = :ecosystemSlug
            """)
    Page<ProjectCategoryReadEntity> findAllByEcosystemSlug(String ecosystemSlug, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT pc.*
            FROM project_categories pc
            JOIN user_profile_info upi ON pc.id = ANY (upi.preferred_category_ids) and upi.id = :userId
            """, nativeQuery = true)
    List<ProjectCategoryReadEntity> findPreferredOnesForUser(UUID userId);
}
