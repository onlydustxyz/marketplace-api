package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.user.domain.model.rbac.Roles;
import onlydust.com.marketplace.user.domain.port.output.UserRoleStoragePort;

import java.util.Optional;
import java.util.Set;

import static onlydust.com.marketplace.user.domain.model.rbac.Roles.*;

@AllArgsConstructor
public class PostgresUserRoleAdapter implements UserRoleStoragePort {
    private final ProjectStoragePort projectStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final SponsorStoragePort sponsorStoragePort;
    private final ProgramStoragePort programStoragePort;
    private final EcosystemStoragePort ecosystemStoragePort;

    private final UserRepository userRepository;

    @Override
    public Set<Roles> findRoles(Optional<UserId> userId, Long contributorId) {
        if (userId.isEmpty()) {
            return Set.of(CONTRIBUTOR_READER);
        }

        final var contributor = userRepository.findByGithubUserId(contributorId);
        if (contributor.isPresent() && contributor.get().getId().equals(userId.get().value())) {
            return Set.of(CONTRIBUTOR_ADMIN);
        }
        return Set.of(CONTRIBUTOR_READER);
    }

    @Override
    public Set<Roles> findRoles(Optional<UserId> userId, ProjectId projectId) {
        if (userId.isPresent() && projectStoragePort.getProjectLeadIds(projectId).contains(userId.get())) {
            return Set.of(PROJECT_MAINTAINER);
        }
        if (projectStoragePort.hasUserAccessToProject(projectId, userId.orElse(null))
            || (userId.isPresent() && programStoragePort.isAdmin(userId.get(), projectId))) {
            return Set.of(PROJECT_READER);
        }
        return Set.of();
    }

    @Override
    public Set<Roles> findRoles(Optional<UserId> userId, ProgramId programId) {
        if (userId.isEmpty()) {
            return Set.of();
        }

        if (userId.isPresent() && programStoragePort.isAdmin(userId.get(), programId)) {
            return Set.of(PROGRAM_ADMIN);
        }
        return Set.of();
    }
}
