package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectPageItemViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProjectsPageRepository extends JpaRepository<ProjectPageItemViewEntity, UUID> {

    @Query(value = """
            select p.project_id,
                   p.hiring,
                   p.logo_url,
                   p.key,
                   p.name,
                   p.short_description,
                   p.long_description,
                   p.visibility,
                   p.rank,
                   (select count(pgr_count.github_repo_id)
                    from public.project_github_repos pgr_count
                    where pgr_count.project_id = p.project_id)  as repo_count,
                   (select count(pc_count.github_user_id) as contributors_count
                    from public.projects_contributors pc_count
                    where pc_count.project_id = p.project_id)  as contributors_count,
                   (select count(pl_count.user_id)
                    from project_leads pl_count
                    where pl_count.project_id = p.project_id) as project_lead_count,
                   false                                      as   is_pending_project_lead,
                   (select jsonb_agg(jsonb_build_object(
                           'url', sponsor.url,
                           'logoUrl', sponsor.logo_url,
                           'id', sponsor.id
                                     ))
                    from sponsors sponsor
                             join public.projects_sponsors ps on ps.project_id = p.project_id
                    where sponsor.id = ps.sponsor_id
                    group by ps.project_id)                     as sponsors,
                   (select json_agg(jsonb_build_object(
                           'id', pl.user_id,
                           'githubId', u.github_user_id,
                           'login', COALESCE(gu.login, u.login_at_signup),
                           'avatarUrl', COALESCE(gu.avatar_url, u.avatar_url_at_signup),
                           'url', gu.html_url
                                    ))
                    from project_leads pl
                             left join auth_users u on u.id = pl.user_id
                             left join github_users gu on gu.id = u.github_user_id
                    where pl.project_id = p.project_id
                    group by pl.project_id)                   as   project_leads,
                   (select json_agg(gr.languages)
                    from project_github_repos pgr
                             left join github_repos gr on gr.id = pgr.github_repo_id
                    where pgr.project_id = p.project_id
                    group by pgr.project_id)                   as  technologies
            from project_details p
            where (select count(github_repo_id)
                   from project_github_repos pgr_count
                   where pgr_count.project_id = p.project_id) > 0
              and p.visibility = 'PUBLIC'
              """, nativeQuery = true)
    Page<ProjectPageItemViewEntity> findProjectsForAnonymousUser(Pageable pageable);
}
