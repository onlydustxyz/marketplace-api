package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.input.ProgramFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;

import java.net.URI;

@AllArgsConstructor
public class ProgramService implements ProgramFacadePort {
    private final ProgramStoragePort programStoragePort;

    @Override
    public Program create(@NonNull String name, @NonNull URI logoUrl) {
        final var program = Program.create(name, logoUrl);
        programStoragePort.save(program);
        return program;
    }
}
