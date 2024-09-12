package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;

import java.util.Optional;

public interface ProgramStoragePort {
    boolean isAdmin(UserId userId, ProgramId programId);

    boolean isAdmin(UserId userId, ProjectId programId);

    void save(Program program);

    boolean isAdmin(UserId userId, String projectSlug);

    Optional<Program> findById(ProgramId programId);
}
