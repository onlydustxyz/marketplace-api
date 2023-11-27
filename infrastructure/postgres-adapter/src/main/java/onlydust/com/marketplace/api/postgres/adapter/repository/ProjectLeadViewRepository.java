package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectLeadViewRepository extends JpaRepository<ProjectLeadViewEntity, Long>,
        JpaSpecificationExecutor<ProjectLeadViewEntity> {

    @Query(value = """
            select
                u.github_user_id,
                u.id,
                u.login,
                u.avatar_url,
                u.html_url,
                true as has_accepted_invitation
            from registered_users u
            join project_leads pl on pl.user_id = u.id and pl.project_id = :projectId
            """, nativeQuery = true)
    List<ProjectLeadViewEntity> findProjectLeaders(UUID projectId);

    @Query(value = """
            (
            select
                u.github_user_id,
                u.id,
                u.login,
                u.avatar_url,
                u.html_url,
                true as has_accepted_invitation
            from registered_users u
            join project_leads pl on pl.user_id = u.id and pl.project_id = :projectId
            )
            UNION
            (
            select
                ga.id as github_user_id,
                NULL as id,
                ga.login,
                ga.avatar_url,
                ga.html_url,
                false as has_accepted_invitation
            from indexer_exp.github_accounts ga
            join pending_project_leader_invitations pli on pli.github_user_id = ga.id and pli.project_id = :projectId
            )
            """, nativeQuery = true)
    List<ProjectLeadViewEntity> findProjectLeadersAndInvitedLeaders(UUID projectId);

}
