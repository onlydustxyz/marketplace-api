package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeSponsorReadApi;
import onlydust.com.backoffice.api.contract.model.SponsorDetailsResponse;
import onlydust.com.backoffice.api.contract.model.SponsorPage;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.repositories.SponsorReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeSponsorsReadApiPostgresAdapter implements BackofficeSponsorReadApi {
    private final SponsorReadRepository sponsorReadRepository;

    @Override
    public ResponseEntity<SponsorDetailsResponse> getSponsor(UUID sponsorId) {
        final var sponsor = sponsorReadRepository.findById(sponsorId)
                .orElseThrow(() -> notFound("Sponsor %s not found".formatted(sponsorId)));

        return ok(sponsor.toBoResponse());
    }

    @Override
    public ResponseEntity<SponsorPage> getSponsors(Integer pageIndex, Integer pageSize, String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = sponsorReadRepository.findAllByName(search, PageRequest.of(index, size, Sort.by("name")));

        final var response = new SponsorPage()
                .sponsors(page.getContent().stream().map(SponsorReadEntity::toBoPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return status(response.getHasMore() ? PARTIAL_CONTENT : OK).body(response);
    }
}
