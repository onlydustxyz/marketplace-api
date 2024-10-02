package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProgramsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.accounting.AllTransactionReadEntity;
import onlydust.com.marketplace.api.read.entities.program.BiFinancialMonthlyStatsReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import onlydust.com.marketplace.api.read.repositories.AllTransactionReadRepository;
import onlydust.com.marketplace.api.read.repositories.BiFinancialMonthlyStatsReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProjectReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.ProgramId;
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
import java.util.Optional;
import java.util.UUID;

import static java.lang.Boolean.FALSE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.api.contract.model.FinancialTransactionType.*;
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
public class ReadProgramsApiPostgresAdapter implements ReadProgramsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final ProgramReadRepository programReadRepository;
    private final PermissionService permissionService;
    private final BiFinancialMonthlyStatsReadRepository biFinancialMonthlyStatsReadRepository;
    private final AllTransactionReadRepository allTransactionReadRepository;
    private final ProjectReadRepository projectReadRepository;

    @Override
    public ResponseEntity<ProgramResponse> getProgram(UUID programId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.hasUserAccessToProgram(authenticatedUser.id(), ProgramId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var program = programReadRepository.findById(programId)
                .orElseThrow(() -> notFound("Program %s not found".formatted(programId)));

        return ok(program.toResponse());
    }

    @Override
    public ResponseEntity<ProgramProjectsPageResponse> getProgramProjects(UUID programId, Integer pageIndex, Integer pageSize, String search) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserProgramLead(authenticatedUser.id(), ProgramId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        int index = sanitizePageIndex(pageIndex);
        int size = sanitizePageSize(pageSize);

        final var page = projectReadRepository.findGrantedProjects(programId, search, PageRequest.of(index, size, Sort.by(Sort.Direction.ASC, "name")));
        final var response = new ProgramProjectsPageResponse()
                .projects(page.getContent().stream().map(p -> p.toProgramProjectPageItemResponse(programId)).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<ProgramProjectResponse> getProgramProject(UUID programId, UUID projectId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserProgramLead(authenticatedUser.id(), ProgramId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final ProgramProjectResponse programProjectResponse = projectReadRepository.findStatsById(projectId)
                .map(p -> p.toProgramProjectResponse(programId))
                .orElseThrow(() -> notFound("Project %s not found for program %s".formatted(projectId, programId)));

        return ResponseEntity.ok(programProjectResponse);
    }

    @Override
    public ResponseEntity<ProgramTransactionPageResponse> getProgramTransactions(UUID programId,
                                                                                 Integer pageIndex,
                                                                                 Integer pageSize,
                                                                                 String fromDate,
                                                                                 String toDate,
                                                                                 List<FinancialTransactionType> types,
                                                                                 String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = findAccountingTransactions(programId, fromDate, toDate, types, search, index, size);

        final var response = new ProgramTransactionPageResponse()
                .transactions(page.getContent().stream().map(AllTransactionReadEntity::toProgramTransactionPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }

    @GetMapping(
            value = "/api/v1/programs/{programId}/transactions",
            produces = "text/csv"
    )
    public ResponseEntity<String> exportProgramTransactions(@PathVariable UUID programId,
                                                            @RequestParam(required = false) Integer pageIndex,
                                                            @RequestParam(required = false) Integer pageSize,
                                                            @RequestParam(required = false) String fromDate,
                                                            @RequestParam(required = false) String toDate,
                                                            @RequestParam(required = false) List<FinancialTransactionType> types,
                                                            @RequestParam(required = false) String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = findAccountingTransactions(programId, fromDate, toDate, types, search, index, size);
        final var format = CSVFormat.DEFAULT.builder().build();
        final var sw = new StringWriter();

        try (final var printer = new CSVPrinter(sw, format)) {
            printer.printRecord("id", "timestamp", "transaction_type", "project_id", "sponsor_id", "amount", "currency", "usd_amount");
            for (final var transaction : page.getContent())
                transaction.toProgramCsv(printer);
        } catch (final IOException e) {
            throw internalServerError("Error while exporting transactions to CSV", e);
        }

        final var csv = sw.toString();

        return status(hasMore(index, page.getTotalPages()) ? PARTIAL_CONTENT : OK)
                .body(csv);
    }

    private Page<AllTransactionReadEntity> findAccountingTransactions(UUID programId, String fromDate, String toDate,
                                                                      List<FinancialTransactionType> types, String search, int index, int size) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserProgramLead(authenticatedUser.id(), ProgramId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));


        return allTransactionReadRepository.findAllForProgram(
                programId,
                DateMapper.parseNullable(fromDate),
                DateMapper.parseNullable(toDate),
                search,
                types == null ? null : types.stream().map(FinancialTransactionType::name).toList(),
                PageRequest.of(index, size, Sort.by("timestamp").descending())
        );
    }

    @Override
    public ResponseEntity<BiFinancialsStatsListResponse> getProgramTransactionsStats(UUID programId,
                                                                                     String fromDate,
                                                                                     String toDate,
                                                                                     List<FinancialTransactionType> types,
                                                                                     String search,
                                                                                     Boolean showEmpty,
                                                                                     FinancialTransactionStatsSort sort,
                                                                                     SortDirection sortDirection) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserProgramLead(authenticatedUser.id(), ProgramId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var comparison = switch (sort) {
            default -> comparing(BiFinancialsStatsResponse::getDate);
        };

        final var allTypes = List.of(ALLOCATED, UNALLOCATED, GRANTED, UNGRANTED);

        final var stats = biFinancialMonthlyStatsReadRepository.findAll(programId,
                        BiFinancialMonthlyStatsReadRepository.IdGrouping.PROGRAM_ID,
                        DateMapper.parseZonedNullable(fromDate),
                        DateMapper.parseZonedNullable(toDate),
                        search,
                        Optional.ofNullable(types).orElse(allTypes).stream().map(Enum::name).toList())
                .stream().collect(groupingBy(BiFinancialMonthlyStatsReadEntity::date));

        final var response = new BiFinancialsStatsListResponse()
                .stats(stats.entrySet().stream().map(e -> new BiFinancialsStatsResponse()
                                        .date(e.getKey().toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                                        .totalAllocated(DetailedTotalMoneyMapper.map(e.getValue(), BiFinancialMonthlyStatsReadEntity::totalAllocated))
                                        .totalGranted(DetailedTotalMoneyMapper.map(e.getValue(), BiFinancialMonthlyStatsReadEntity::totalGranted))
                                        .totalRewarded(DetailedTotalMoneyMapper.map(e.getValue(), BiFinancialMonthlyStatsReadEntity::totalRewarded))
                                        .transactionCount(e.getValue().stream().mapToInt(BiFinancialMonthlyStatsReadEntity::transactionCount).sum())
                                )
                                .filter(s -> !FALSE.equals(showEmpty) || s.getTransactionCount() > 0)
                                .sorted(sortDirection == SortDirection.ASC ? comparison : comparison.reversed())
                                .toList()
                );

        return ok(response);
    }
}
