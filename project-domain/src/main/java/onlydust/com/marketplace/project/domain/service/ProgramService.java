package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.input.ProgramFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class ProgramService implements ProgramFacadePort {
    private final ProgramStoragePort programStoragePort;

    @Override
    @Transactional
    public Program create(@NonNull String name, @NonNull SponsorId sponsorId, URI url, URI logoUrl, @NonNull List<UserId> leadIds) {
        final var program = Program.create(name, sponsorId, leadIds, url, logoUrl);
        programStoragePort.save(program);
        return program;
    }

    @Override
    @Transactional
    public void update(@NonNull UUID programId, @NonNull String name, URI url, URI logoUrl, @NonNull List<UserId> leadIds) {
        final var program = programStoragePort.findById(ProgramId.of(programId))
                .orElseThrow(() -> notFound("Program %s not found".formatted(programId)));
        programStoragePort.save(program.toBuilder()
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .leadIds(leadIds)
                .build());
    }
}
