package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.project.ProjectEcosystemCardReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectEcosystemCardReadEntityRepository extends JpaRepository<ProjectEcosystemCardReadEntity, UUID> {

    @Query(nativeQuery = true, value = """
                with contributors as (select pc.project_id, ga.*, row_number() over (partition by pc.project_id order by pc.total_contribution_count desc) rank
                                      from projects_contributors pc
                                               join indexer_exp.github_accounts ga on ga.id = pc.github_user_id),
                     has_gfi as (select project_id, count(issue_id) > 0 as exist from projects_good_first_issues group by project_id)
                select p.id,
                       p.name,
                       p.slug,
                       p.short_description,
                       p.logo_url,
                       cc.users                                                                                   top_contributors,
                       (select count(distinct github_user_id) from projects_contributors where project_id = p.id) contributors_count,
                       coalesce((select jsonb_agg(
                                       jsonb_build_object(
                                               'id', l.id, 'name', l.name, 'slug', l.slug, 'logoUrl', l.logo_url, 'bannerUrl', l.banner_url
                                       ) order by l.name
                               )
                        from project_languages pl
                        join languages l on pl.language_id = l.id
                        where pl.project_id = p.id), '[]')                                                                languages
                from ecosystems e
                         join projects_ecosystems pe on pe.ecosystem_id = e.id
                         join public_projects p on p.id = pe.project_id
                         left join (select c.project_id,
                                           jsonb_agg(
                                                   jsonb_build_object(
                                                           'githubUserId', c.id, 'login', c.login, 'avatarUrl', c.avatar_url
                                                   ) order by c.rank, c.name
                                           ) users
                                    from contributors c
                                    where c.rank <= 3
                                    group by c.project_id ) cc on cc.project_id = p.id
                        left join has_gfi on has_gfi.project_id = p.id
                        left join (select p_tags.project_id, jsonb_agg(jsonb_build_object('name', p_tags.tag)) names
                                    from projects_tags p_tags
                                    group by p_tags.project_id) tags on tags.project_id = p.id
                where e.slug = :ecosystemSlug
                and ( :hasGoodFirstIssues is null or has_gfi.exist = :hasGoodFirstIssues)
                and ( :featuredProjectsOnly is null or pe.featured_rank is not null)
                and ( :tagJsonPath is null or jsonb_path_exists(tags.names, cast(cast(:tagJsonPath as text) as jsonpath )))
                order by case
                    when :featuredProjectsOnly is not null then (pe.featured_rank, UPPER(p.name))
                    when cast(:orderBy as text) = 'RANK' then (-p.rank, UPPER(p.name))
                    else (0, UPPER(p.name))
                end
                offset :offset limit :limit
            """)
    List<ProjectEcosystemCardReadEntity> findAllBy(String ecosystemSlug, Boolean hasGoodFirstIssues, Boolean featuredProjectsOnly, int offset,
                                                   int limit, String orderBy, String tagJsonPath);

    @Query(nativeQuery = true, value = """
                    with has_gfi as (select project_id, count(issue_id) > 0 as exist from projects_good_first_issues group by project_id)
                    select count(distinct p.id)
                            from ecosystems e
                            join projects_ecosystems pe on pe.ecosystem_id = e.id
                            join public_projects p on p.id = pe.project_id
                            left join has_gfi on has_gfi.project_id = p.id
                            left join (select p_tags.project_id, jsonb_agg(jsonb_build_object('name', p_tags.tag)) names
                                    from projects_tags p_tags
                                    group by p_tags.project_id) tags on tags.project_id = p.id
                            where e.slug = :ecosystemSlug
                            and ( :hasGoodFirstIssues is null or has_gfi.exist = :hasGoodFirstIssues)
                            and ( :tagJsonPath is null or jsonb_path_exists(tags.names, cast(cast(:tagJsonPath as text) as jsonpath )))
            """)
    int countAllBy(String ecosystemSlug, Boolean hasGoodFirstIssues, String tagJsonPath);
}
