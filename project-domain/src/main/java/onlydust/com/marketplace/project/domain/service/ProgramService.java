package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.input.ProgramFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@AllArgsConstructor
public class ProgramService implements ProgramFacadePort {
    private final ProgramStoragePort programStoragePort;

    @Override
    @Transactional
    public Program create(@NonNull String name, URI url, URI logoUrl, UserId leadId) {
        final var program = Program.create(name, url, logoUrl);
        programStoragePort.save(program);
        if (leadId != null)
            addLead(program.id(), leadId);
        return program;
    }

    @Override
    public void addLead(@NonNull ProgramId programId, @NonNull UserId leadId) {
        programStoragePort.saveProgramLead(programId, leadId);
    }
}
