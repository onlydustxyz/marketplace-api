package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.Sponsor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.project.domain.port.output.ProjectSponsorStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresSponsorAdapter implements SponsorStoragePort, ProjectSponsorStoragePort {
    private final SponsorRepository sponsorRepository;
    private final SponsorUserRepository sponsorUserRepository;

    @Override
    @Transactional
    public boolean isAdmin(UserId userId, SponsorId sponsorId) {
        return sponsorUserRepository.findById(new SponsorUserEntity.PrimaryKey(userId.value(), sponsorId.value()))
                .isPresent();
    }

    @Override
    @Transactional
    public boolean isUserSponsorAdmin(UUID id, UUID sponsorId) {
        return isAdmin(UserId.of(id), SponsorId.of(sponsorId));
    }

    @Override
    @Transactional
    public void addLeadToSponsor(UserId leadId, SponsorId sponsorId) {
        sponsorUserRepository.save(new SponsorUserEntity(leadId.value(), sponsorId.value()));
    }

    @Override
    public Optional<Sponsor> get(SponsorId sponsorId) {
        return sponsorRepository.findById(sponsorId.value())
                .map(SponsorEntity::toAccountingDomain);
    }
}
