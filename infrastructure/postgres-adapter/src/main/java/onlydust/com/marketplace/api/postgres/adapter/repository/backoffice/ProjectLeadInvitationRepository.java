package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.ProjectLeadInvitationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectLeadInvitationRepository extends JpaRepository<ProjectLeadInvitationEntity, UUID> {

    @Query(value = """
            SELECT ppli.id, ppli.project_id, ppli.github_user_id
            FROM pending_project_leader_invitations ppli
            WHERE (COALESCE(:ids) IS NULL OR ppli.id IN (:ids))
            """, nativeQuery = true)
    Page<ProjectLeadInvitationEntity> findAllByIds(final Pageable pageable, final List<UUID> ids);
}
