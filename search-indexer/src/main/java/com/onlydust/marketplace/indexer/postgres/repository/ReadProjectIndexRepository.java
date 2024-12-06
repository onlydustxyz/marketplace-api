package com.onlydust.marketplace.indexer.postgres.repository;

import com.onlydust.marketplace.indexer.postgres.entity.SearchProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReadProjectIndexRepository extends JpaRepository<SearchProjectEntity, UUID> {
    @Query(value = """
                select p.id,
                       p.name,
                       p.short_description,
                       p.long_description,
                       (select distinct jsonb_agg(jsonb_build_object('name', l.name))
                        from project_github_repos pgr
                                 join repo_languages rl on rl.repo_id = pgr.github_repo_id
                                 join languages l on l.id = rl.language_id
                        where pgr.project_id = p.id) languages,
                       (select distinct jsonb_agg(jsonb_build_object('name', e.name))
                        from ecosystems e
                                 join projects_ecosystems pe on pe.ecosystem_id = e.id
                        where pe.project_id = p.id) ecosystems,
                        (select distinct jsonb_agg(jsonb_build_object('name', pc.name))
                        from project_categories pc
                                 join projects_project_categories ppc on ppc.project_category_id = pc.id
                        where ppc.project_id = p.id) categories
                  from projects p
            """, nativeQuery = true)
    List<SearchProjectEntity> findAll();
}