package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProgramLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProgramLeadRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class PostgresProgramAdapter implements ProgramStoragePort {
    private final ProgramLeadRepository programLeadRepository;

    @Override
    public boolean isAdmin(UUID userId, ProgramId programId) {
        return programLeadRepository.findById(new ProgramLeadEntity.PrimaryKey(userId, programId.value()))
                .isPresent();
    }
}
