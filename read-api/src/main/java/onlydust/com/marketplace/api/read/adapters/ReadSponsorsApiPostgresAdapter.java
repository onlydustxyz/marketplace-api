package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadSponsorsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.accounting.AccountBookTransactionReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.repositories.AccountBookTransactionReadRepository;
import onlydust.com.marketplace.api.read.repositories.SponsorReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadSponsorsApiPostgresAdapter implements ReadSponsorsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final AccountBookTransactionReadRepository accountBookTransactionReadRepository;
    private final SponsorReadRepository sponsorReadRepository;

    @Override
    public ResponseEntity<SponsorResponse> getSponsor(UUID sponsorId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), sponsorId))
            throw forbidden("User %s is not admin of sponsor %s".formatted(authenticatedUser.id(), sponsorId));

        final var sponsor = sponsorReadRepository.findById(sponsorId)
                .map(SponsorReadEntity::toResponse)
                .orElseThrow(() -> forbidden("Sponsor %s not found".formatted(sponsorId)));

        return ok(sponsor);
    }

    @Override
    public ResponseEntity<TransactionHistoryPageResponse> getSponsorTransactionHistory(UUID sponsorId,
                                                                                       Integer pageIndex,
                                                                                       Integer pageSize,
                                                                                       String fromDate,
                                                                                       String toDate,
                                                                                       List<UUID> currencies,
                                                                                       List<UUID> projects,
                                                                                       List<SponsorAccountTransactionType> types,
                                                                                       SponsorAccountTransactionSort sort,
                                                                                       SortDirection direction) {

        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), sponsorId))
            throw forbidden("User %s is not admin of sponsor %s".formatted(authenticatedUser.id(), sponsorId));

        final var sortBy = switch (Optional.ofNullable(sort).orElse(SponsorAccountTransactionSort.DATE)) {
            case DATE -> "timestamp";
            case TYPE -> "type";
            case AMOUNT -> "amount";
            case PROJECT -> "p.name";
        };

        final var page = accountBookTransactionReadRepository.findAllFromSponsor(
                sponsorId,
                types == null ? null : types.stream().map(AccountBookTransactionReadEntity::map).toList(),
                currencies,
                projects,
                DateMapper.parseNullable(fromDate),
                DateMapper.parseNullable(toDate),
                PageRequest.of(pageIndex, pageSize, Sort.by(direction == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy))
        );

        final var response = new TransactionHistoryPageResponse()
                .transactions(page.getContent().stream().map(AccountBookTransactionReadEntity::toPageItemResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }
}
