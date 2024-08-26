package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;

import java.net.URI;

public interface ProgramFacadePort {
    Program create(@NonNull String name, URI url, URI logoUrl, UserId leadId);

    void addLead(@NonNull ProgramId programId, @NonNull UserId leadId);
}
