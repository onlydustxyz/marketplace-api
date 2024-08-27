package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProgramLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProgramEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProgramLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProgramRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresProgramAdapter implements ProgramStoragePort {
    private final ProgramRepository programRepository;
    private final ProgramLeadRepository programLeadRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UUID userId, ProgramId programId) {
        return programLeadRepository.findById(new ProgramLeadEntity.PrimaryKey(userId, programId.value()))
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UUID userId, ProjectId projectId) {
        return programLeadRepository.findByProjectId(userId, projectId.value())
                .isPresent();
    }

    @Override
    @Transactional
    public void save(Program program) {
        programRepository.save(ProgramEntity.of(program));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UUID userId, String projectSlug) {
        return programLeadRepository.findByProjectSlug(userId, projectSlug)
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Program> findById(ProgramId programId) {
        return programRepository.findById(programId.value())
                .map(ProgramEntity::toDomain);
    }
}
