package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorFacadePort;
import onlydust.com.marketplace.api.contract.SponsorsApi;
import onlydust.com.marketplace.api.contract.model.SponsorAccountTransactionType;
import onlydust.com.marketplace.api.contract.model.SponsorDetailsResponse;
import onlydust.com.marketplace.api.contract.model.TransactionHistoryPageResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SponsorMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "Sponsors"))
@AllArgsConstructor
@Slf4j
public class SponsorsRestApi implements SponsorsApi {
    private final SponsorFacadePort sponsorFacadePort;
    private final AccountingFacadePort accountingFacadePort;
    private final AuthenticatedAppUserService authenticatedAppUserService;

    @Override
    public ResponseEntity<SponsorDetailsResponse> getSponsor(UUID sponsorId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sponsor = sponsorFacadePort.getSponsor(UserId.of(authenticatedUser.getId()), SponsorId.of(sponsorId));
        final var sponsorAccountStatements = accountingFacadePort.getSponsorAccounts(SponsorId.of(sponsorId));
        return ResponseEntity.ok(SponsorMapper.mapToSponsorDetailsResponse(sponsor, sponsorAccountStatements));

    }

    @Override
    public ResponseEntity<TransactionHistoryPageResponse> getSponsorTransactionHistory(UUID sponsorId,
                                                                                       Integer pageIndex,
                                                                                       Integer pageSize,
                                                                                       String fromDate,
                                                                                       String toDate,
                                                                                       List<UUID> currencies,
                                                                                       List<UUID> projects,
                                                                                       List<SponsorAccountTransactionType> types) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var sponsor = sponsorFacadePort.getSponsor(UserId.of(authenticatedUser.getId()), SponsorId.of(sponsorId));

        final var filters = HistoricalTransaction.Filters.builder()
                .currencies(Optional.ofNullable(currencies).orElse(List.of()).stream().map(Currency.Id::of).toList())
                .projectIds(Optional.ofNullable(projects).orElse(List.of()).stream().map(ProjectId::of).toList())
                .types(Optional.ofNullable(types).orElse(List.of(SponsorAccountTransactionType.values())).stream().map(SponsorMapper::map).toList())
                .from(isNull(fromDate) ? null : DateMapper.parse(fromDate))
                .to(isNull(toDate) ? null : DateMapper.parse(toDate))
                .build();

        final var page = accountingFacadePort.transactionHistory(
                sponsor.id(),
                filters,
                sanitizedPageIndex,
                sanitizePageSize(pageSize)
        );
        final var response = SponsorMapper.mapTransactionHistory(page, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }
}
