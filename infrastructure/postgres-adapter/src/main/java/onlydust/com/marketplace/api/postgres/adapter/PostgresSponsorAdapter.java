package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingSponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.output.SponsorStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresSponsorAdapter implements SponsorStoragePort, AccountingSponsorStoragePort {
    private final SponsorRepository sponsorRepository;
    private final SponsorUserRepository sponsorUserRepository;

    @Override
    @Transactional
    public boolean isAdmin(UUID userId, Sponsor.Id sponsorId) {
        return sponsorUserRepository.findById(new SponsorUserEntity.PrimaryKey(userId, sponsorId.value()))
                .isPresent();
    }

    @Override
    @Transactional
    public void addLeadToSponsor(UUID leadId, Sponsor.Id sponsorId) {
        sponsorUserRepository.save(new SponsorUserEntity(leadId, sponsorId.value()));
    }

    @Override
    public Optional<Sponsor> get(Sponsor.Id sponsorId) {
        return sponsorRepository.findById(sponsorId.value())
                .map(SponsorEntity::toDomain);
    }

    @Override
    public void save(Sponsor sponsor) {
        sponsorRepository.save(SponsorEntity.of(sponsor));
    }

    @Override
    public Optional<SponsorView> get(SponsorId sponsorId) {
        return sponsorRepository.findById(sponsorId.value())
                .map(SponsorEntity::toView);
    }
}
