package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.project.ProjectCategoryPageItemReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ProjectCategoryPageItemReadRepository extends Repository<ProjectCategoryPageItemReadEntity, UUID> {
    @Query(value = """
            SELECT
                pcs.id      as id,
                pcs.name    as name,
                null        as icon_slug,
                'PENDING'   as status,
                null        as project_count
            FROM
                project_category_suggestions pcs
            UNION
            SELECT
                pc.id        as id,
                pc.name      as name,
                pc.icon_slug as icon_slug,
                'APPROVED'   as status,
                0            as project_count
            FROM
                project_categories pc
            ORDER BY
                status DESC, name ASC
            """,
            countQuery = """
                    WITH suggestions as (SELECT count(*) as total FROM project_category_suggestions pcs),
                         categories as (SELECT count(*) as total FROM project_categories pc)
                    SELECT
                        suggestions.total + categories.total
                    FROM
                        suggestions, categories
                    """, nativeQuery = true)
    Page<ProjectCategoryPageItemReadEntity> findAll(Pageable pageable);
}
