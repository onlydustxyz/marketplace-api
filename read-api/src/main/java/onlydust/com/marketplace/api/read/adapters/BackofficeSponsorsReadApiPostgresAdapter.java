package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeSponsorReadApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.repositories.DepositReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.api.read.repositories.SponsorReadRepository;
import onlydust.com.marketplace.kernel.model.blockchain.MetaBlockExplorer;
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
    private final ProgramReadRepository programReadRepository;
    private final DepositReadRepository depositReadRepository;
    private final MetaBlockExplorer blockExplorer;

    @Override
    public ResponseEntity<BoDepositResponse> getDepositDetails(UUID depositId) {
        final var deposit = depositReadRepository.findById(depositId)
                .orElseThrow(() -> notFound("Deposit %s not found".formatted(depositId)));
        return ok(deposit.toBoResponse(blockExplorer));
    }

    @Override
    public ResponseEntity<ProgramDetailsResponse> getProgram(UUID programId) {
        final var program = programReadRepository.findById(programId)
                .orElseThrow(() -> notFound("Program %s not found".formatted(programId)));

        return ok(program.toBoDetailsResponse());
    }

    @Override
    public ResponseEntity<SponsorDetailsResponse> getSponsor(UUID sponsorId) {
        final var sponsor = sponsorReadRepository.findById(sponsorId)
                .orElseThrow(() -> notFound("Sponsor %s not found".formatted(sponsorId)));

        return ok(sponsor.toBoDetailsResponse());
    }

    @Override
    public ResponseEntity<DepositPage> getSponsorDeposits(UUID sponsorId, Integer pageIndex, Integer pageSize) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = depositReadRepository.findAllBySponsorId(sponsorId, PageRequest.of(index, size, Sort.by("transaction.timestamp").descending()));

        final var response = new DepositPage()
                .deposits(page.getContent().stream().map(d -> d.toBoPageItemResponse(blockExplorer)).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return status(response.getHasMore() ? PARTIAL_CONTENT : OK).body(response);
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
