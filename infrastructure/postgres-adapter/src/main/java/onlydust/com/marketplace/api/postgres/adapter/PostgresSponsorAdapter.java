package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.SponsorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorViewRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.port.output.ProjectSponsorStoragePort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresSponsorAdapter implements SponsorStoragePort, ProjectSponsorStoragePort {
    private final SponsorViewRepository sponsorViewRepository;

    @Override
    @Transactional
    public Page<SponsorView> findSponsors(String search, int pageIndex, int pageSize) {
        final var page = sponsorViewRepository.findAllByNameContainingIgnoreCase(search == null ? "" : search,
                PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return Page.<SponsorView>builder()
                .content(page.getContent().stream().map(SponsorViewEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public Optional<SponsorView> get(SponsorId sponsorId) {
        return sponsorViewRepository.findById(sponsorId.value()).map(SponsorViewEntity::toView);
    }

    @Override
    @Transactional
    public boolean isAdmin(UserId userId, SponsorId sponsorId) {
        return sponsorViewRepository.findById(sponsorId.value())
                .map(s -> s.getUsers().stream().anyMatch(u -> u.id().equals(userId.value())))
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean isUserSponsorAdmin(UUID id, UUID sponsorId) {
        return isAdmin(UserId.of(id), SponsorId.of(sponsorId));
    }
}
