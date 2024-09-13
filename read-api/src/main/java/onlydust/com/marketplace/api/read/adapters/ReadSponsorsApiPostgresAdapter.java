package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadSponsorsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.accounting.AllTransactionReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.program.SponsorTransactionMonthlyStatReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadSponsorsApiPostgresAdapter implements ReadSponsorsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final AllTransactionReadRepository allTransactionReadRepository;
    private final SponsorReadRepository sponsorReadRepository;
    private final ProgramReadRepository programReadRepository;
    private final SponsorTransactionMonthlyStatsReadRepository sponsorTransactionMonthlyStatsReadRepository;
    private final DepositReadRepository depositReadRepository;

    @Override
    public ResponseEntity<DepositResponse> getDeposit(UUID depositId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var deposit = depositReadRepository.findById(depositId)
                .orElseThrow(() -> notFound("Deposit %s not found".formatted(depositId)));

        final var sponsorId = SponsorId.of(deposit.sponsor().id());
        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), sponsorId))
            throw unauthorized("User %s is not admin of sponsor %s".formatted(authenticatedUser.id(), sponsorId));

        return ok(deposit.toResponse());
    }

    @Override
    public ResponseEntity<SponsorResponse> getSponsor(UUID sponsorId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), SponsorId.of(sponsorId)))
            throw unauthorized("User %s is not admin of sponsor %s".formatted(authenticatedUser.id(), sponsorId));

        final var sponsor = sponsorReadRepository.findById(sponsorId).orElseThrow(() -> notFound("Sponsor %s not found".formatted(sponsorId)));

        return ok(sponsor.toResponse());
    }

    @Override
    public ResponseEntity<SponsorTransactionPageResponse> getSponsorTransactions(UUID sponsorId,
                                                                                 Integer pageIndex,
                                                                                 Integer pageSize,
                                                                                 String fromDate,
                                                                                 String toDate,
                                                                                 List<SponsorTransactionType> types,
                                                                                 String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = findAccountBookTransactions(sponsorId, fromDate, toDate, types, search, index, size);

        final var response = new SponsorTransactionPageResponse()
                .transactions(page.getContent().stream().map(AllTransactionReadEntity::toSponsorTransactionPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }


    @GetMapping(value = "/api/v1/sponsors/{sponsorId}/transactions", produces = "text/csv")
    public ResponseEntity<String> exportSponsorTransactions(@PathVariable UUID sponsorId,
                                                            @RequestParam(required = false) Integer pageIndex,
                                                            @RequestParam(required = false) Integer pageSize,
                                                            @RequestParam(required = false) String fromDate,
                                                            @RequestParam(required = false) String toDate,
                                                            @RequestParam(required = false) List<SponsorTransactionType> types,
                                                            @RequestParam(required = false) String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = findAccountBookTransactions(sponsorId, fromDate, toDate, types, search, index, size);
        final var format = CSVFormat.DEFAULT.builder().build();
        final var sw = new StringWriter();

        try (final var printer = new CSVPrinter(sw, format)) {
            printer.printRecord("id", "timestamp", "transaction_type", "deposit_status", "program_id", "amount", "currency", "usd_amount");
            for (final var transaction : page.getContent())
                transaction.toSponsorCsv(printer);
        } catch (final IOException e) {
            throw internalServerError("Error while exporting transactions to CSV", e);
        }

        final var csv = sw.toString();

        return status(hasMore(index, page.getTotalPages()) ? PARTIAL_CONTENT : OK).body(csv);
    }

    private Page<AllTransactionReadEntity> findAccountBookTransactions(UUID sponsorId,
                                                                       String fromDate,
                                                                       String toDate,
                                                                       List<SponsorTransactionType> types,
                                                                       String search,
                                                                       int index,
                                                                       int size) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), SponsorId.of(sponsorId)))
            throw unauthorized("User %s is not authorized to access sponsor %s".formatted(authenticatedUser.id(), sponsorId));


        return allTransactionReadRepository.findAllForSponsor(sponsorId, DateMapper.parseNullable(fromDate),
                DateMapper.parseNullable(toDate), search, types == null ? null : types.stream().map(SponsorTransactionType::name).toList(),
                PageRequest.of(index, size, Sort.by(Sort.Order.by("timestamp"), Sort.Order.by("depositStatus").nullsLast())));
    }

    @Override
    public ResponseEntity<SponsorTransactionStatListResponse> getSponsorTransactionsStats(UUID sponsorId, String fromDate, String toDate,
                                                                                          List<SponsorTransactionType> types, String search) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), SponsorId.of(sponsorId)))
            throw unauthorized("User %s is not authorized to access sponsor %s".formatted(authenticatedUser.id(), sponsorId));

        final var stats = sponsorTransactionMonthlyStatsReadRepository.findAll(sponsorId,
                        toZoneDateTime(DateMapper.parseNullable(fromDate)),
                        toZoneDateTime(DateMapper.parseNullable(toDate)),
                        search,
                        types == null ? null : types.stream().map(SponsorTransactionType::name).toList())
                .stream()
                .collect(groupingBy(SponsorTransactionMonthlyStatReadEntity::date));

        final var response = new SponsorTransactionStatListResponse()
                .stats(stats.entrySet().stream().map(e -> new SponsorTransactionStatResponse()
                                .date(e.getKey().toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                                .totalAvailable(DetailedTotalMoneyMapper.map(e.getValue(), SponsorTransactionMonthlyStatReadEntity::totalAvailable))
                                .totalAllocated(DetailedTotalMoneyMapper.map(e.getValue(), SponsorTransactionMonthlyStatReadEntity::totalAllocated))
                                .totalGranted(DetailedTotalMoneyMapper.map(e.getValue(), SponsorTransactionMonthlyStatReadEntity::totalGranted))
                                .totalRewarded(DetailedTotalMoneyMapper.map(e.getValue(), SponsorTransactionMonthlyStatReadEntity::totalRewarded))
                                .transactionCount(e.getValue().stream().mapToInt(SponsorTransactionMonthlyStatReadEntity::transactionCount).sum()))
                        .sorted(comparing(SponsorTransactionStatResponse::getDate))
                        .toList());

        return ok(response);
    }

    @Override
    public ResponseEntity<SponsorProgramPageResponse> getSponsorPrograms(UUID sponsorId, Integer pageIndex, Integer pageSize, String search) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), SponsorId.of(sponsorId)))
            throw unauthorized("User %s is not authorized to access sponsor %s".formatted(authenticatedUser.id(), sponsorId));

        int index = sanitizePageIndex(pageIndex);
        int size = sanitizePageSize(pageSize);

        final var page = programReadRepository.findSponsorPrograms(sponsorId, search, PageRequest.of(index, size, Sort.by(Sort.Direction.ASC, "name")));
        final var response = new SponsorProgramPageResponse()
                .programs(page.getContent().stream().map(ProgramReadEntity::toSponsorProgramPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }
}
