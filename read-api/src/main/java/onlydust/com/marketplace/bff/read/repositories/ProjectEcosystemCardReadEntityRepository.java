package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.project.ProjectEcosystemCardReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectEcosystemCardReadEntityRepository extends JpaRepository<ProjectEcosystemCardReadEntity, UUID> {

    @Query(nativeQuery = true, value = """
                with contributors as (select pc.project_id, ga.*, row_number() over (partition by pc.project_id) rank
                                      from projects_contributors pc
                                               join indexer_exp.github_accounts ga on ga.id = pc.github_user_id
                                      order by pc.total_contribution_count desc)
                select p.id,
                       p.name,
                       p.slug,
                       p.short_description,
                       p.logo_url,
                       cc.users                                                                                   top_contributors,
                       (select count(distinct github_user_id) from projects_contributors where project_id = p.id) contributors_count,
                       (select jsonb_agg(
                                       jsonb_build_object(
                                               'id', l.id, 'name', l.name, 'logoUrl', l.logo_url, 'bannerUrl', l.banner_url
                                       )
                               )
                        from project_languages pl
                        join languages l on pl.language_id = l.id
                        where pl.project_id = p.id)                                                                languages
                from ecosystems e
                         join projects_ecosystems pe on pe.ecosystem_id = e.id
                         join projects p on p.id = pe.project_id
                         left join (select c.project_id,
                                           jsonb_agg(
                                                   jsonb_build_object(
                                                           'githubUserId', c.id, 'login', c.login, 'avatarUrl', c.avatar_url
                                                   )
                                           ) users
                                    from contributors c
                                    where c.rank <= 3
                                    group by c.project_id) cc on cc.project_id = p.id
                         left join (SELECT pgr.project_id, count(i.id) > 0 exist
                                    FROM project_github_repos pgr
                                             join indexer_exp.github_issues i on i.repo_id = pgr.github_repo_id
                                             LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
                                             JOIN LATERAL (
                                        SELECT issue_id
                                        FROM indexer_exp.github_issues_labels gil
                                                 JOIN indexer_exp.github_labels gl ON gil.label_id = gl.id
                                        WHERE gil.issue_id = i.id
                                          AND gl.name ilike '%good%first%issue%'
                                        LIMIT 1
                                        ) gfi ON gfi.issue_id = i.id
                                    WHERE i.status = 'OPEN'
                                      AND gia.user_id IS NULL
                                    group by pgr.project_id) has_gfi on has_gfi.project_id = p.id
                where e.slug = :ecosystemSlug
                and ( :hasGoodFirstIssues is null or has_gfi.exist = :hasGoodFirstIssues)
                offset :offset limit :limit
            """)
    List<ProjectEcosystemCardReadEntity> findAllBy(String ecosystemSlug, Boolean hasGoodFirstIssues, int offset,
                                                   int limit);

    @Query(nativeQuery = true, value = """
                    select count(distinct p.id)
                             from ecosystems e
                             join projects_ecosystems pe on pe.ecosystem_id = e.id
                             join projects p on p.id = pe.project_id
                             left join (SELECT pgr.project_id, count(i.id) > 0 exist
                                        FROM project_github_repos pgr
                                                 join indexer_exp.github_issues i on i.repo_id = pgr.github_repo_id
                                                 LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
                                                 JOIN LATERAL (
                                            SELECT issue_id
                                            FROM indexer_exp.github_issues_labels gil
                                                     JOIN indexer_exp.github_labels gl ON gil.label_id = gl.id
                                            WHERE gil.issue_id = i.id
                                              AND gl.name ilike '%good%first%issue%'
                                            LIMIT 1
                                            ) gfi ON gfi.issue_id = i.id
                                        WHERE i.status = 'OPEN'
                                          AND gia.user_id IS NULL
                                        group by pgr.project_id) has_gfi on has_gfi.project_id = p.id
                            where e.slug = :ecosystemSlug
                            and ( :hasGoodFirstIssues is null or has_gfi.exist = :hasGoodFirstIssues)
            """)
    int countAllBy(String ecosystemSlug, Boolean hasGoodFirstIssues);
}
