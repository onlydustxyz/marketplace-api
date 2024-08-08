package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingReadApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.read.entities.accounting.AllSponsorAccountTransactionReadEntity;
import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardStatusReadEntity;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.read.entities.accounting.AllSponsorAccountTransactionReadEntity.Type.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeAccountingReadApiPostgresAdapter implements BackofficeAccountingReadApi {
    private final SponsorAccountReadRepository sponsorAccountReadRepository;
    private final AccountingFacadePort accountingFacadePort;
    private final BatchPaymentReadRepository batchPaymentReadRepository;
    private final AllSponsorAccountTransactionReadRepository allSponsorAccountTransactionReadRepository;
    private final RewardReadRepository rewardReadRepository;
    private final BillingProfileReadRepository billingProfileReadRepository;

    @Override
    public ResponseEntity<BatchPaymentDetailsResponse> getBatchPayment(UUID batchPaymentId) {
        final var payment = batchPaymentReadRepository.findById(batchPaymentId)
                .orElseThrow(() -> notFound("Batch payment %s not found".formatted(batchPaymentId)));

        return ok(payment.toDetailsResponse());
    }

    @Override
    public ResponseEntity<BatchPaymentPageResponse> getBatchPayments(Integer pageIndex, Integer pageSize, List<BatchPaymentStatus> statuses) {
        final var page = batchPaymentReadRepository.findAllByStatusIn(statuses, PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending()));

        final var response = new BatchPaymentPageResponse()
                .batchPayments(page.getContent().stream().map(BatchPaymentReadEntity::toResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<BillingProfileResponse> getBillingProfilesById(UUID billingProfileId) {
        final var billingProfile = billingProfileReadRepository.findById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        return ok(billingProfile.toBoResponse());
    }

    @Override
    public ResponseEntity<RewardPageResponse> getRewards(Integer pageIndex,
                                                         Integer pageSize,
                                                         List<RewardStatusContract> statuses,
                                                         List<UUID> billingProfiles,
                                                         List<Long> recipients,
                                                         List<UUID> projects,
                                                         String fromRequestedAt,
                                                         String toRequestedAt,
                                                         String fromProcessedAt,
                                                         String toProcessedAt) {
        final var page = rewardReadRepository.find(
                statuses == null ? null : statuses.stream().map(RewardStatusReadEntity::of).toList(),
                billingProfiles,
                recipients,
                projects,
                DateMapper.parseNullable(fromRequestedAt),
                DateMapper.parseNullable(toRequestedAt),
                DateMapper.parseNullable(fromProcessedAt),
                DateMapper.parseNullable(toProcessedAt),
                PageRequest.of(pageIndex, pageSize, Sort.by("requestedAt").descending()));

        final var response = new RewardPageResponse()
                .rewards(page.getContent().stream().map(RewardReadEntity::toBoPageItemResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<AccountListResponse> getSponsorAccounts(UUID sponsorId) {
        final var accounts = sponsorAccountReadRepository.findAllBySponsorId(sponsorId, Sort.by("currency.code"));

        final var response = new AccountListResponse()
                .accounts(accounts.stream().map(account -> {
                            final var sponsorAccountStatement = accountingFacadePort.getSponsorAccountStatement(SponsorAccount.Id.of(account.id()))
                                    .orElseThrow(() -> internalServerError("Sponsor account %s not found".formatted(account.id())));
                            return account.toDto(sponsorAccountStatement);
                        })
                        .toList());

        return ok(response);
    }

    @Override
    public ResponseEntity<TransactionHistoryPageResponse> getSponsorTransactionHistory(UUID sponsorId, Integer pageIndex, Integer pageSize) {
        final var page = allSponsorAccountTransactionReadRepository.findAll(
                sponsorId,
                List.of(WITHDRAW, DEPOSIT, TRANSFER, REFUND),
                PageRequest.of(pageIndex, pageSize, Sort.by("timestamp").descending())
        );

        final var response = new TransactionHistoryPageResponse()
                .transactions(page.getContent().stream().map(AllSponsorAccountTransactionReadEntity::toBoResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }
}
