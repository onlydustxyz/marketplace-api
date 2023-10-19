package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectLeaderInvitationRepository extends JpaRepository<ProjectLeaderInvitationEntity, UUID> {
    Optional<ProjectLeaderInvitationEntity> findByProjectIdAndGithubUserId(UUID projectId, Long githubUserId);
}
