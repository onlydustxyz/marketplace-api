package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Program;

import java.util.UUID;

public interface ProgramStoragePort {
    boolean isAdmin(UUID userId, Program.Id programId);
}
