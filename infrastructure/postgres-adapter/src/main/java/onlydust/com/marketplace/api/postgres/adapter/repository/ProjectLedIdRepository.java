package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectLedIdRepository extends JpaRepository<ProjectLedIdQueryEntity, UUID> {

    @Query(value = """
              (select pl.user_id,
                    pl.project_id,
                    p.slug as project_slug,
                    p.name,
                    p.logo_url,
                    false     pending,
                     (select count(pc.github_user_id)
                      from projects_contributors pc
                      where pc.project_id = p.id) contributor_count,
                   (select count(pgr.github_repo_id) > count(agr.repo_id)
                           from project_github_repos pgr
                                    join indexer_exp.github_repos gr2 on gr2.id = pgr.github_repo_id
                                    left join indexer_exp.authorized_github_repos agr on agr.repo_id = pgr.github_repo_id
                           where pgr.project_id = p.id and gr2.visibility = 'PUBLIC')        as is_missing_github_app_installation
             from project_leads pl
                      join projects p on p.id = pl.project_id
             where pl.user_id = :userId and (select count(pgr2.github_repo_id)
                                                from public.project_github_repos pgr2
                                                join indexer_exp.github_repos gr on pgr2.github_repo_id = gr.id
                                                where pgr2.project_id = p.id
                                                and gr.visibility = 'PUBLIC' ) > 0)
            union
            (select u.id,
                    p.id as project_id,
                    p.slug as project_slug,
                    p.name,
                    p.logo_url,
                    true      pending,
                     (select count(pc.github_user_id)
                      from projects_contributors pc
                      where pc.project_id = p.id) contributor_count,
                 (select count(pgr.github_repo_id) > count(agr.repo_id)
                           from project_github_repos pgr
                                    join indexer_exp.github_repos gr2 on gr2.id = pgr.github_repo_id
                                    left join indexer_exp.authorized_github_repos agr on agr.repo_id = pgr.github_repo_id
                           where pgr.project_id = p.id and gr2.visibility = 'PUBLIC')        as is_missing_github_app_installation
             from iam.users u
                      join pending_project_leader_invitations ppli on ppli.github_user_id = u.github_user_id
                      join projects p on p.id = ppli.project_id
             where u.id = :userId and (select count(pgr2.github_repo_id)
                                           from public.project_github_repos pgr2
                                           join indexer_exp.github_repos gr on pgr2.github_repo_id = gr.id
                                                where pgr2.project_id = p.id
                                                and gr.visibility = 'PUBLIC') > 0)""", nativeQuery = true)
    List<ProjectLedIdQueryEntity> findProjectLedIdsByUserId(final UUID userId);
}
