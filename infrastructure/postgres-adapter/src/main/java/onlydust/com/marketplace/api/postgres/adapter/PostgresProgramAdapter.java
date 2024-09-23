package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProgramLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProgramEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProgramLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProgramRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresProgramAdapter implements ProgramStoragePort {
    private final ProgramRepository programRepository;
    private final ProgramLeadRepository programLeadRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UserId userId, ProgramId programId) {
        return programLeadRepository.findById(new ProgramLeadEntity.PrimaryKey(userId.value(), programId.value()))
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UserId userId, ProjectId projectId) {
        return programLeadRepository.findByProjectId(userId.value(), projectId.value())
                .isPresent();
    }

    @Override
    @Transactional
    public void save(Program program) {
        programRepository.save(ProgramEntity.of(program));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UserId userId, String projectSlug) {
        return programLeadRepository.findByProjectSlug(userId.value(), projectSlug)
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Program> findById(ProgramId programId) {
        return programRepository.findById(programId.value())
                .map(ProgramEntity::toDomain);
    }

    @Override
    public List<UserId> findProgramLeads(@NonNull ProgramId programId) {
        return programLeadRepository.findByProgramId(programId.value()).stream()
                .map(ProgramLeadEntity::userId)
                .map(UserId::of)
                .toList();
    }

    @Override
    public List<ProgramId> getProgramLedIdsForUser(UserId userId) {
        return programLeadRepository.findByUserId(userId.value()).stream()
                .map(ProgramLeadEntity::programId)
                .map(ProgramId::of)
                .toList();
    }
}
