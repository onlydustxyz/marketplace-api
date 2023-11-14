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
                    false     pending
             from project_leads pl
                      join project_details pd on pd.project_id = pl.project_id
             where pl.user_id = :userId)
            union
            (select au.id,
                    pd.project_id,
                    pd.key as project_slug,
                    pd.name,
                    pd.logo_url,
                    true      pending
             from auth_users au
                      join pending_project_leader_invitations ppli on ppli.github_user_id = au.github_user_id
                      join project_details pd on pd.project_id = ppli.project_id
             where au.id = :userId)""", nativeQuery = true)
    List<ProjectLedIdViewEntity> findProjectLedIdsByUserId(final UUID userId);
}
