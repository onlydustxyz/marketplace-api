package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;

import java.util.List;
import java.util.Optional;

public interface ProgramStoragePort {
    boolean isAdmin(UserId userId, ProgramId programId);

    boolean isAdmin(UserId userId, ProjectId programId);

    void save(Program program);

    boolean isAdmin(UserId userId, String projectSlug);

    Optional<Program> findById(ProgramId programId);

    List<UserId> findProgramLeads(@NonNull ProgramId programId);

    List<ProgramId> getProgramLedIdsForUser(UserId userId);
}
