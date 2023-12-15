package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectLedIdRepository extends JpaRepository<ProjectLedIdViewEntity, UUID> {

    @Query(value = """
              (select pl.user_id,
                    pl.project_id,
                    pd.key as project_slug,
                    pd.name,
                    pd.logo_url,
                    false     pending,
                     (select count(pc.github_user_id)
                      from projects_contributors pc
                      where pc.project_id = pd.project_id) contributor_count
             from project_leads pl
                      join project_details pd on pd.project_id = pl.project_id
             where pl.user_id = :userId and (select count(pgr2.github_repo_id)
                                                from public.project_github_repos pgr2
                                                join indexer_exp.github_repos gr on pgr2.github_repo_id = gr.id
                                                where pgr2.project_id = pd.project_id
                                                and gr.visibility = 'PUBLIC' ) > 0)
            union
            (select u.id,
                    pd.project_id,
                    pd.key as project_slug,
                    pd.name,
                    pd.logo_url,
                    true      pending,
                     (select count(pc.github_user_id)
                      from projects_contributors pc
                      where pc.project_id = pd.project_id) contributor_count
             from iam.users u
                      join pending_project_leader_invitations ppli on ppli.github_user_id = u.github_user_id
                      join project_details pd on pd.project_id = ppli.project_id
             where u.id = :userId and (select count(pgr2.github_repo_id)
                                           from public.project_github_repos pgr2
                                           join indexer_exp.github_repos gr on pgr2.github_repo_id = gr.id
                                                where pgr2.project_id = pd.project_id
                                                and gr.visibility = 'PUBLIC') > 0)""", nativeQuery = true)
    List<ProjectLedIdViewEntity> findProjectLedIdsByUserId(final UUID userId);
}
