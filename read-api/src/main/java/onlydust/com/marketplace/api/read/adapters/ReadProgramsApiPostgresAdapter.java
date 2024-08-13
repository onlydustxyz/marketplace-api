package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.AccountingPermissionService;
import onlydust.com.marketplace.api.contract.ReadProgramsApi;
import onlydust.com.marketplace.api.contract.model.ProgramResponse;
import onlydust.com.marketplace.api.contract.model.ProgramTransactionStatListResponse;
import onlydust.com.marketplace.api.contract.model.ProgramTransactionStatResponse;
import onlydust.com.marketplace.api.contract.model.TransactionType;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionMonthlyStatReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStat;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramTransactionMonthlyStatsReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramTransactionStatsReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.springframework.context.annotation.Profile;
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
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadProgramsApiPostgresAdapter implements ReadProgramsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final ProgramReadRepository programReadRepository;
    private final AccountingPermissionService accountingPermissionService;
    private final ProgramTransactionStatsReadRepository programTransactionStatsReadRepository;
    private final ProgramTransactionMonthlyStatsReadRepository programTransactionMonthlyStatsReadRepository;

    @Override
    public ResponseEntity<ProgramResponse> getProgram(UUID programId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var program = programReadRepository.findById(programId)
                .orElseThrow(() -> notFound("Program %s not found".formatted(programId)));

        final var stats = programTransactionStatsReadRepository.findAll(programId);

        return ok(program.toResponse()
                .totalAvailable(DetailedTotalMoneyMapper.map(stats, ProgramTransactionStat::totalAvailable))
                .totalGranted(DetailedTotalMoneyMapper.map(stats, ProgramTransactionStat::totalGranted))
                .totalRewarded(DetailedTotalMoneyMapper.map(stats, ProgramTransactionStat::totalRewarded))
        );
    }

    @Override
    public ResponseEntity<ProgramTransactionStatListResponse> getProgramTransactionsStats(UUID programId,
                                                                                          String fromDate,
                                                                                          String toDate,
                                                                                          List<TransactionType> types,
                                                                                          String search) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var stats = programTransactionMonthlyStatsReadRepository.findAll(
                        programId,
                        DateMapper.parseNullable(fromDate),
                        DateMapper.parseNullable(toDate))
                .stream().collect(groupingBy(ProgramTransactionMonthlyStatReadEntity::date));

        final var response = new ProgramTransactionStatListResponse()
                .stats(stats.entrySet().stream().map(e -> new ProgramTransactionStatResponse()
                                        .date(e.getKey().toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                                        .totalAvailable(DetailedTotalMoneyMapper.map(e.getValue(), ProgramTransactionStat::totalAvailable))
                                        .totalGranted(DetailedTotalMoneyMapper.map(e.getValue(), ProgramTransactionStat::totalGranted))
                                        .totalRewarded(DetailedTotalMoneyMapper.map(e.getValue(), ProgramTransactionStat::totalRewarded))
                                )
                                .sorted(comparing(ProgramTransactionStatResponse::getDate))
                                .toList()
                );

        return ok(response);
    }
}
