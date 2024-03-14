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
                u.github_login as login,
                user_avatar_url(u.github_user_id, u.github_avatar_url) as avatar_url,
                ga.html_url,
                true as has_accepted_invitation
            from iam.users u
            join project_leads pl on pl.user_id = u.id and pl.project_id = :projectId
            left join indexer_exp.github_accounts ga on ga.id = u.github_user_id
            """, nativeQuery = true)
    List<ProjectLeadViewEntity> findProjectLeaders(UUID projectId);

    @Query(value = """
            (
            select
                u.github_user_id,
                u.id,
                u.github_login as login,
                user_avatar_url(u.github_user_id, u.github_avatar_url) as avatar_url,
                ga.html_url,
                true as has_accepted_invitation
            from iam.users u
            join project_leads pl on pl.user_id = u.id and pl.project_id = :projectId
            left join indexer_exp.github_accounts ga on ga.id = u.github_user_id
            )
            UNION
            (
            select
                ga.id as github_user_id,
                NULL as id,
                ga.login,
                user_avatar_url(ga.id, ga.avatar_url) as avatar_url,
                ga.html_url,
                false as has_accepted_invitation
            from indexer_exp.github_accounts ga
            join pending_project_leader_invitations pli on pli.github_user_id = ga.id and pli.project_id = :projectId
            )
            """, nativeQuery = true)
    List<ProjectLeadViewEntity> findProjectLeadersAndInvitedLeaders(UUID projectId);

}
