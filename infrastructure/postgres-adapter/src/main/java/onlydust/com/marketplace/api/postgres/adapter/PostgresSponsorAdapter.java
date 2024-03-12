package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.SponsorViewRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresSponsorAdapter implements SponsorStoragePort {
    private final SponsorViewRepository sponsorViewRepository;

    @Override
    public Page<SponsorView> findSponsors(int pageIndex, int pageSize) {
        final var page = sponsorViewRepository.findAll(PageRequest.of(pageIndex, pageSize));
        return Page.<SponsorView>builder()
                .content(page.getContent().stream().map(entity ->
                        SponsorView.builder()
                                .id(entity.getId())
                                .name(entity.getName())
                                .url(entity.getUrl())
                                .logoUrl(entity.getLogoUrl())
                                .projects(isNull(entity.getProjects()) ? List.of() : entity.getProjects().stream()
                                        .map(p -> ShortProjectView.builder()
                                                .slug(p.getSlug())
                                                .name(p.getName())
                                                .logoUrl(p.getLogoUrl())
                                                .shortDescription(p.getShortDescription())
                                                .id(ProjectId.of(p.getId()))
                                                .build()
                                        ).toList())
                                .build()
                ).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
