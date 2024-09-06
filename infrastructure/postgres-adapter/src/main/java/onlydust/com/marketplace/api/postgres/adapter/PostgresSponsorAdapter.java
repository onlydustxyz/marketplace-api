package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingSponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.output.SponsorStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresSponsorAdapter implements SponsorStoragePort, AccountingSponsorStoragePort {
    private final SponsorRepository sponsorRepository;
    private final SponsorLeadRepository sponsorLeadRepository;

    @Override
    @Transactional
    public boolean isAdmin(UUID userId, SponsorId sponsorId) {
        return sponsorLeadRepository.findById(new SponsorLeadEntity.PrimaryKey(userId, sponsorId.value()))
                .isPresent();
    }

    @Override
    @Transactional
    public boolean isAdminOfAnySponsor(UUID userId) {
        return sponsorLeadRepository.findByUserId(userId).isPresent();
    }

    @Override
    @Transactional
    public boolean isAdminOfProgramSponsor(UUID userId, ProgramId programId) {
        return sponsorLeadRepository.findByUserIdAndProgramId(userId, programId.value()).isPresent();
    }

    @Override
    @Transactional
    public void addLeadToSponsor(UUID leadId, SponsorId sponsorId) {
        sponsorLeadRepository.save(new SponsorLeadEntity(leadId, sponsorId.value()));
    }

    @Override
    public Optional<Sponsor> get(SponsorId sponsorId) {
        return sponsorRepository.findById(sponsorId.value())
                .map(SponsorEntity::toDomain);
    }

    @Override
    public List<UserId> findSponsorLeads(SponsorId sponsorId) {
        return sponsorLeadRepository.findBySponsorId(sponsorId.value()).stream()
                .map(SponsorLeadEntity::getUserId)
                .map(UserId::of)
                .toList();
    }

    @Override
    @Transactional
    public void save(Sponsor sponsor) {
        sponsorRepository.save(SponsorEntity.of(sponsor));
    }

    @Override
    public Optional<SponsorView> getView(SponsorId sponsorId) {
        return sponsorRepository.findById(sponsorId.value())
                .map(SponsorEntity::toView);
    }
}
