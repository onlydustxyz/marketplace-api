package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.AccountingPermissionService;
import onlydust.com.marketplace.api.contract.ReadProgramsApi;
import onlydust.com.marketplace.api.contract.model.ProgramResponse;
import onlydust.com.marketplace.api.contract.model.ProgramTransactionStatListResponse;
import onlydust.com.marketplace.api.contract.model.TransactionType;
import onlydust.com.marketplace.api.read.entities.accounting.SponsorAccountReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStatReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramTransactionStatsReadRepository;
import onlydust.com.marketplace.api.read.repositories.SponsorAccountReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
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
    private final SponsorAccountReadRepository sponsorAccountReadRepository;

    @Override
    public ResponseEntity<ProgramResponse> getProgram(UUID programId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        final var accounts = sponsorAccountReadRepository.findAllBySponsorId(programId, Sort.by("id"))
                .stream().map(SponsorAccountReadEntity::id)
                .toList();

        final var program = programReadRepository.findById(programId)
                .orElseThrow(() -> notFound("Program %s not found".formatted(programId)));

        final var stats = programTransactionStatsReadRepository.findAll(accounts)
                .stream()
                // TODO remove when migrated to programs
                .collect(groupingBy(ProgramTransactionStatReadEntity::accountBookId, reducing(null, ProgramTransactionStatReadEntity::merge)))
                .values();

        return ok(program.toResponse()
                .totalAvailable(DetailedTotalMoneyMapper.map(stats, ProgramTransactionStatReadEntity::totalAvailable))
                .totalGranted(DetailedTotalMoneyMapper.map(stats, ProgramTransactionStatReadEntity::totalGranted))
                .totalRewarded(DetailedTotalMoneyMapper.map(stats, ProgramTransactionStatReadEntity::totalRewarded))
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

//        final var stats = programTransactionStatsReadRepository.findAll(programId);
        return ReadProgramsApi.super.getProgramTransactionsStats(programId, fromDate, toDate, types, search);
    }
}
