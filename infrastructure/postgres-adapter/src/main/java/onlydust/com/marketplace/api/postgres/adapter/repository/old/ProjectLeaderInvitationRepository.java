package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLeaderInvitationRepository extends JpaRepository<ProjectLeaderInvitationEntity, UUID> {

  Optional<ProjectLeaderInvitationEntity> findByProjectIdAndGithubUserId(UUID projectId, Long githubUserId);

  Set<ProjectLeaderInvitationEntity> findAllByProjectId(UUID projectId);
}
