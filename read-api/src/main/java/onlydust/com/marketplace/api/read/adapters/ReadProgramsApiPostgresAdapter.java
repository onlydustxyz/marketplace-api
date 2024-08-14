package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.AccountingPermissionService;
import onlydust.com.marketplace.api.contract.ReadProgramsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.accounting.AccountBookTransactionReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionMonthlyStatReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectStatReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import onlydust.com.marketplace.api.read.repositories.AccountBookTransactionReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramTransactionMonthlyStatsReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProjectStatsReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.unauthorized;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
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
    private final AccountingPermissionService accountingPermissionService;
    private final ProgramTransactionMonthlyStatsReadRepository programTransactionMonthlyStatsReadRepository;
    private final AccountBookTransactionReadRepository accountBookTransactionReadRepository;
    private final ProjectStatsReadRepository projectStatsReadRepository;

    @Override
    public ResponseEntity<ProgramResponse> getProgram(UUID programId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var program = programReadRepository.findById(programId)
                .orElseThrow(() -> notFound("Program %s not found".formatted(programId)));

        return ok(program.toResponse());
    }

    @Override
    public ResponseEntity<ProgramProjectsPageResponse> getProgramProjects(UUID programId, Integer pageIndex, Integer pageSize) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        int index = sanitizePageIndex(pageIndex);
        int size = sanitizePageSize(pageSize);

        final var page = projectStatsReadRepository.findGrantedProject(programId, PageRequest.of(index, size));
        final var response = new ProgramProjectsPageResponse()
                .projects(page.getContent().stream().map(ProjectStatReadEntity::toProgramProjectPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<TransactionPageResponse> getProgramTransactions(UUID programId,
                                                                          Integer pageIndex,
                                                                          Integer pageSize,
                                                                          String fromDate,
                                                                          String toDate,
                                                                          List<ProgramTransactionType> types,
                                                                          String search) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = accountBookTransactionReadRepository.findAllForProgram(
                programId,
                DateMapper.parseNullable(fromDate),
                DateMapper.parseNullable(toDate),
                search,
                types == null ? null : types.stream().map(ProgramTransactionType::name).toList(),
                PageRequest.of(index, size, Sort.by("timestamp"))
        );

        final var response = new TransactionPageResponse()
                .transactions(page.getContent().stream().map(AccountBookTransactionReadEntity::toProgramTransactionPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<ProgramTransactionStatListResponse> getProgramTransactionsStats(UUID programId,
                                                                                          String fromDate,
                                                                                          String toDate,
                                                                                          List<ProgramTransactionType> types,
                                                                                          String search) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var stats = programTransactionMonthlyStatsReadRepository.findAll(
                        programId,
                        DateMapper.parseNullable(fromDate),
                        DateMapper.parseNullable(toDate),
                        search,
                        types == null ? null : types.stream().map(ProgramTransactionType::name).toList())
                .stream().collect(groupingBy(ProgramTransactionMonthlyStatReadEntity::date));

        final var response = new ProgramTransactionStatListResponse()
                .stats(stats.entrySet().stream().map(e -> new ProgramTransactionStatResponse()
                                        .date(e.getKey().toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                                        .totalAvailable(DetailedTotalMoneyMapper.map(e.getValue(), ProgramTransactionMonthlyStatReadEntity::totalAvailable))
                                        .totalGranted(DetailedTotalMoneyMapper.map(e.getValue(), ProgramTransactionMonthlyStatReadEntity::totalGranted))
                                        .totalRewarded(DetailedTotalMoneyMapper.map(e.getValue(), ProgramTransactionMonthlyStatReadEntity::totalRewarded))
                                        .transactionCount(e.getValue().stream().mapToInt(ProgramTransactionMonthlyStatReadEntity::transactionCount).sum())
                                )
                                .sorted(comparing(ProgramTransactionStatResponse::getDate))
                                .toList()
                );

        return ok(response);
    }
}
