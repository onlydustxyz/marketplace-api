package com.onlydust.marketplace.indexer.postgres.repository;

import com.onlydust.marketplace.indexer.postgres.entity.SearchProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReadSearchProjectRepository extends JpaRepository<SearchProjectEntity, UUID> {
    @Query(value = """
                select p.id,
                       p.name,
                       p.short_description,
                       p.slug,
                       (select distinct jsonb_agg(jsonb_build_object('name', l.name))
                        from project_github_repos pgr
                                 join repo_languages rl on rl.repo_id = pgr.github_repo_id
                                 join languages l on l.id = rl.language_id
                        where pgr.project_id = p.id) languages,
                       (select distinct jsonb_agg(jsonb_build_object('name', e.name))
                        from ecosystems e
                                 join projects_ecosystems pe on pe.ecosystem_id = e.id
                        where pe.project_id = p.id)  ecosystems,
                       (select distinct jsonb_agg(jsonb_build_object('name', pc.name))
                        from project_categories pc
                                 join projects_project_categories ppc on ppc.project_category_id = pc.id
                        where ppc.project_id = p.id) categories,
                       coalesce((select ppcd.contributor_count from bi.p_project_contributions_data ppcd where ppcd.project_id = p.id),
                                0)                   contributor_count,
                       sum(gr.forks_count)           fork_count,
                       sum(gr.stars_count)           star_count
                from projects p
                         left join project_github_repos pgr on pgr.project_id = p.id
                         left join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                where p.visibility = 'PUBLIC'
                group by p.id, p.name, p.short_description, p.long_description, p.slug
            """, nativeQuery = true)
    List<SearchProjectEntity> findAll();
}