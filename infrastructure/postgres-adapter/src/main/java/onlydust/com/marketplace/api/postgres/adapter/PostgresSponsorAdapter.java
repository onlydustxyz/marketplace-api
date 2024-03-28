package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresSponsorAdapter implements SponsorStoragePort {
    private final SponsorRepository sponsorRepository;

    @Override
    @Transactional
    public Page<SponsorView> findSponsors(String search, int pageIndex, int pageSize) {
        final var page = sponsorRepository.findAllByNameContainingIgnoreCase(search == null ? "" : search,
                PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return Page.<SponsorView>builder()
                .content(page.getContent().stream().map(SponsorEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public Optional<SponsorView> get(SponsorId sponsorId) {
        return sponsorRepository.findById(sponsorId.value()).map(SponsorEntity::toView);
    }
}
