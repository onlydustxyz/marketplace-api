package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProgramLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProgramEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProgramLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProgramRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class PostgresProgramAdapter implements ProgramStoragePort {
    private final ProgramRepository programRepository;
    private final ProgramLeadRepository programLeadRepository;

    @Override
    public boolean isAdmin(UUID userId, ProgramId programId) {
        return programLeadRepository.findById(new ProgramLeadEntity.PrimaryKey(userId, programId.value()))
                .isPresent();
    }

    @Override
    public boolean isAdmin(UUID userId, ProjectId projectId) {
        return programLeadRepository.findByProjectId(userId, projectId.value())
                .isPresent();
    }

    @Override
    public void save(Program program) {
        programRepository.save(ProgramEntity.of(program));
    }

    @Override
    public void saveProgramLead(ProgramId programId, UserId leadId) {
        programLeadRepository.save(ProgramLeadEntity.of(programId, leadId));
    }

    @Override
    public boolean isAdmin(UUID userId, String projectSlug) {
        return programLeadRepository.findByProjectSlug(userId, projectSlug)
                .isPresent();
    }
}