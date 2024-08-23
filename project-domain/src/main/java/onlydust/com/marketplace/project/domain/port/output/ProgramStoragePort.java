package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProgramId;

import java.util.UUID;

public interface ProgramStoragePort {
    boolean isAdmin(UUID userId, ProgramId programId);
}
