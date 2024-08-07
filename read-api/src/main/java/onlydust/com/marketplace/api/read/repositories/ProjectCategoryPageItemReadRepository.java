package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryPageItemReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ProjectCategoryPageItemReadRepository extends Repository<ProjectCategoryPageItemReadEntity, UUID> {
    @Query(value = """
            SELECT
                pcs.id      as id,
                null        as slug,
                pcs.name    as name,
                null        as description,
                null        as icon_slug,
                'PENDING'   as status,
                null        as project_count
            FROM
                project_category_suggestions pcs
            UNION
            SELECT
                pc.id                           as id,
                pc.slug                         as slug,
                pc.name                         as name,
                pc.description                  as description,
                pc.icon_slug                    as icon_slug,
                'APPROVED'                      as status,
                count(distinct ppc.project_id)  as project_count
            FROM
                project_categories pc
                LEFT JOIN projects_project_categories ppc ON ppc.project_category_id = pc.id
            GROUP BY
                pc.id
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
