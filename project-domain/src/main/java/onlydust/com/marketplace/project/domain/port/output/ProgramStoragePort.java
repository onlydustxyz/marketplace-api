package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Program;

import java.util.Optional;
import java.util.UUID;

public interface ProgramStoragePort {
    boolean isAdmin(UUID userId, ProgramId programId);

    boolean isAdmin(UUID userId, ProjectId programId);

    void save(Program program);

    boolean isAdmin(UUID userId, String projectSlug);

    Optional<Program> findById(ProgramId programId);
}
