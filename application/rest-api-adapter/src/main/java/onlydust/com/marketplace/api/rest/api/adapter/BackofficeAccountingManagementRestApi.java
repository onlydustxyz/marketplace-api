package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedBackofficeUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BatchPaymentMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeAccountingManagement"))
@AllArgsConstructor
public class BackofficeAccountingManagementRestApi implements BackofficeAccountingManagementApi {
    private final AccountingFacadePort accountingFacadePort;
    private final AccountingRewardPort accountingRewardPort;
    private final PaymentPort paymentPort;
    private final BillingProfileFacadePort billingProfileFacadePort;
    private final AuthenticatedBackofficeUserService authenticatedBackofficeUserService;
    private final BlockchainFacadePort blockchainFacadePort;

    @Override
    public ResponseEntity<AccountResponse> createSponsorAccount(UUID sponsorUuid, CreateAccountRequest createAccountRequest) {
        final var sponsorId = SponsorId.of(sponsorUuid);
        final var currencyId = Currency.Id.of(createAccountRequest.getCurrencyId());
        final var lockedUntil = createAccountRequest.getLockedUntil();
        final var allowance = createAccountRequest.getAllowance() == null ? null : PositiveAmount.of(createAccountRequest.getAllowance());
        final var transaction = createAccountRequest.getReceipt() == null ? null : mapReceiptToTransaction(createAccountRequest.getReceipt());

        if (allowance == null && transaction == null)
            throw badRequest("Either allowance or transaction must be set");

        if (allowance != null && transaction != null)
            throw badRequest("Both allowance and transaction cannot be set at the same time");

        final var sponsorAccountStatement = transaction == null ?
                accountingFacadePort.createSponsorAccountWithInitialAllowance(sponsorId, currencyId, lockedUntil, allowance) :
                accountingFacadePort.createSponsorAccountWithInitialBalance(sponsorId, currencyId, lockedUntil, transaction);

        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountListResponse> getSponsorAccounts(UUID sponsorId) {
        final var sponsorAccounts = accountingFacadePort.getSponsorAccounts(SponsorId.of(sponsorId));
        return ResponseEntity.ok(new AccountListResponse().accounts(sponsorAccounts.stream().map(BackOfficeMapper::mapAccountToResponse).toList()));
    }

    @Override
    public ResponseEntity<TransactionHistoryPageResponse> getSponsorTransactionHistory(UUID sponsorId, Integer pageIndex, Integer pageSize) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var page = accountingFacadePort.transactionHistory(
                SponsorId.of(sponsorId),
                HistoricalTransaction.Filters.builder()
                        .types(List.of(HistoricalTransaction.Type.DEPOSIT,
                                HistoricalTransaction.Type.WITHDRAW,
                                HistoricalTransaction.Type.TRANSFER,
                                HistoricalTransaction.Type.REFUND))
                        .build(),
                sanitizedPageIndex,
                sanitizePageSize(pageSize),
                HistoricalTransaction.Sort.DATE,
                SortDirection.desc);
        final var response = mapTransactionHistory(page, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TransactionReceipt> registerTransactionReceipt(UUID accountId, RegisterTransactionReceiptRequest registerTransactionReceiptRequest) {
        final var receipt = registerTransactionReceiptRequest.getReceipt();
        final var sponsorAccountStatement = accountingFacadePort.fund(SponsorAccount.Id.of(accountId), mapReceiptToTransaction(receipt));
        final var addedReceipt = sponsorAccountStatement.account().getTransactions().stream()
                .filter(t -> t.reference().equals(receipt.getReference())).findFirst().orElseThrow();
        return ResponseEntity.ok(mapTransactionToReceipt(sponsorAccountStatement.account(), addedReceipt));
    }

    @Override
    public ResponseEntity<AccountResponse> removeTransactionReceipt(UUID accountId, UUID receiptId) {
        final var sponsorAccountStatement = accountingFacadePort.delete(
                SponsorAccount.Id.of(accountId),
                SponsorAccount.Transaction.Id.of(receiptId));
        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountResponse> updateAccountAllowance(UUID accountId, UpdateAccountAllowanceRequest updateAccountAllowanceRequest) {
        final var sponsorAccountStatement = accountingFacadePort.increaseAllowance(SponsorAccount.Id.of(accountId),
                Amount.of(updateAccountAllowanceRequest.getAllowance()));
        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountResponse> updateAccountAttributes(UUID accountId, UpdateAccountRequest updateAccountRequest) {
        final var sponsorAccountStatement = accountingFacadePort.updateSponsorAccount(SponsorAccount.Id.of(accountId), updateAccountRequest.getLockedUntil());
        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<Void> allocateBudgetToProject(UUID projectId, ProjectBudgetAllocationRequest request) {
        final var sponsorAccount = accountingFacadePort.getSponsorAccount(SponsorAccount.Id.of(request.getSponsorAccountId()))
                .orElseThrow(() -> badRequest("Sponsor account %s not found".formatted(request.getSponsorAccountId())));

        accountingFacadePort.allocate(SponsorAccount.Id.of(request.getSponsorAccountId()),
                ProjectId.of(projectId),
                PositiveAmount.of(request.getAmount()),
                sponsorAccount.currency().id());

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unallocateBudgetFromProject(UUID projectId, ProjectBudgetAllocationRequest request) {
        final var sponsorAccount = accountingFacadePort.getSponsorAccount(SponsorAccount.Id.of(request.getSponsorAccountId()))
                .orElseThrow(() -> badRequest("Sponsor account %s not found".formatted(request.getSponsorAccountId())));

        accountingFacadePort.unallocate(ProjectId.of(projectId),
                SponsorAccount.Id.of(request.getSponsorAccountId()),
                PositiveAmount.of(request.getAmount()),
                sponsorAccount.currency().id());

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> payReward(UUID rewardId, PayRewardRequest payRewardRequest) {
        final var network = mapTransactionNetwork(payRewardRequest.getNetwork());
        final var transactionTimestamp = blockchainFacadePort.getTransactionTimestamp(
                network.blockchain().orElseThrow(),
                payRewardRequest.getReference());

        accountingFacadePort.pay(
                RewardId.of(rewardId),
                transactionTimestamp,
                network,
                payRewardRequest.getReference());

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RewardPageResponse> getRewards(Integer pageIndex, Integer pageSize,
                                                         List<RewardStatusContract> statuses,
                                                         List<UUID> billingProfiles,
                                                         List<Long> recipients,
                                                         String fromRequestedAt,
                                                         String toRequestedAt,
                                                         String fromProcessedAt,
                                                         String toProcessedAt) {
        final var authenticatedUser = authenticatedBackofficeUserService.getAuthenticatedBackofficeUser().asAuthenticatedUser();

        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final Page<RewardDetailsView> rewards = accountingRewardPort.getRewards(
                sanitizedPageIndex,
                sanitizedPageSize,
                statuses != null ? statuses.stream().map(BackOfficeMapper::map).toList() : null,
                billingProfiles != null ? billingProfiles.stream().map(BillingProfile.Id::of).toList() : null,
                recipients != null ? recipients.stream().map(GithubUserId::of).toList() : null,
                DateMapper.parseNullable(fromRequestedAt),
                DateMapper.parseNullable(toRequestedAt),
                DateMapper.parseNullable(fromProcessedAt),
                DateMapper.parseNullable(toProcessedAt)
        );
        return ResponseEntity.ok(rewardPageToResponse(sanitizedPageIndex, rewards, authenticatedUser));
    }

    @Override
    public ResponseEntity<String> exportRewardsCSV(List<RewardStatusContract> statuses,
                                                   List<UUID> billingProfiles,
                                                   String fromRequestedAt,
                                                   String toRequestedAt,
                                                   String fromProcessedAt,
                                                   String toProcessedAt) {
        if (fromRequestedAt == null && toRequestedAt == null && fromProcessedAt == null && toProcessedAt == null)
            throw badRequest("At least one of the date filters must be set");

        final String csv = accountingRewardPort.exportRewardsCSV(
                Optional.ofNullable(statuses).orElse(List.of()).stream().map(BackOfficeMapper::map).toList(),
                billingProfiles != null ? billingProfiles.stream().map(BillingProfile.Id::of).toList() : null,
                DateMapper.parseNullable(fromRequestedAt),
                DateMapper.parseNullable(toRequestedAt),
                DateMapper.parseNullable(fromProcessedAt),
                DateMapper.parseNullable(toProcessedAt)
        );
        return ResponseEntity.ok(csv);
    }

    @Override
    public ResponseEntity<RewardDetailsResponse> getReward(UUID rewardId) {
        final var authenticatedUser = authenticatedBackofficeUserService.getAuthenticatedBackofficeUser().asAuthenticatedUser();
        final var reward = accountingRewardPort.getReward(RewardId.of(rewardId));
        return ResponseEntity.ok(map(reward, authenticatedUser));
    }

    @Override
    public ResponseEntity<BatchPaymentsResponse> createBatchPayments(PostBatchPaymentRequest postBatchPaymentRequest) {
        final var batchPayments =
                paymentPort.createPaymentsForInvoices(postBatchPaymentRequest.getInvoiceIds().stream().map(Invoice.Id::of).toList());

        return ResponseEntity.ok(BatchPaymentMapper.domainToResponse(
                paymentPort.findPaymentsByIds(batchPayments.stream().map(Payment::id).collect(Collectors.toSet()))
        ));
    }

    @Override
    public ResponseEntity<Void> updateBatchPayment(UUID batchPaymentId, BatchPaymentRequest batchPaymentRequest) {
        paymentPort.markPaymentAsPaid(Payment.Id.of(batchPaymentId), batchPaymentRequest.getTransactionHash());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BatchPaymentDetailsResponse> getBatchPayment(UUID batchPaymentId) {
        final var authenticatedUser = authenticatedBackofficeUserService.getAuthenticatedBackofficeUser().asAuthenticatedUser();
        return ResponseEntity.ok(BatchPaymentMapper.domainToDetailedResponse(paymentPort.findPaymentById(Payment.Id.of(batchPaymentId)), authenticatedUser));
    }

    @Override
    public ResponseEntity<Void> deleteBatchPayment(UUID batchPaymentId) {
        paymentPort.deletePaymentById(Payment.Id.of(batchPaymentId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BatchPaymentPageResponse> getBatchPayments(Integer pageIndex, Integer pageSize, List<BatchPaymentStatus> statuses) {
        final int sanitizePageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final int sanitizedPageSize = PaginationHelper.sanitizePageSize(pageSize);
        final BatchPaymentPageResponse batchPaymentPageResponse = BatchPaymentMapper.pageToResponse(
                paymentPort.findPayments(sanitizePageIndex, sanitizedPageSize, statuses == null ? null :
                        statuses.stream().map(BatchPaymentMapper::map).collect(Collectors.toSet())),
                pageIndex);
        return batchPaymentPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(batchPaymentPageResponse) :
                ResponseEntity.ok(batchPaymentPageResponse);
    }

    @Override
    public ResponseEntity<Void> notifyRewardsPaid() {
        accountingRewardPort.notifyAllNewPaidRewards();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<BillingProfileResponse> getBillingProfilesById(UUID billingProfileId) {
        return ResponseEntity.ok(map(billingProfileFacadePort.getById(BillingProfile.Id.of(billingProfileId))));
    }

    @Override
    public ResponseEntity<EarningsResponse> getEarnings(List<RewardStatusContract> statuses,
                                                        List<Long> recipients,
                                                        List<UUID> billingProfiles,
                                                        List<UUID> projects,
                                                        String fromRequestedAt,
                                                        String toRequestedAt,
                                                        String fromProcessedAt,
                                                        String toProcessedAt) {
        final EarningsView earnings = accountingRewardPort.getEarnings(
                statuses != null ? statuses.stream().map(BackOfficeMapper::map).toList() : null,
                recipients != null ? recipients.stream().map(GithubUserId::of).toList() : null,
                billingProfiles != null ? billingProfiles.stream().map(BillingProfile.Id::of).toList() : null,
                projects != null ? projects.stream().map(ProjectId::of).toList() : null,
                DateMapper.parseNullable(fromRequestedAt),
                DateMapper.parseNullable(toRequestedAt),
                DateMapper.parseNullable(fromProcessedAt),
                DateMapper.parseNullable(toProcessedAt)
        );
        return ResponseEntity.ok(new EarningsResponse()
                .totalUsdAmount(prettyUsd(earnings.totalUsdAmount()))
                .amountsPerCurrency(earnings.earningsPerCurrencies().stream()
                        .sorted(Comparator.comparing(a -> a.money().currency().code()))
                        .map(earningsPerCurrency -> new EarningsResponseAmountsPerCurrencyInner()
                                .rewardCount(earningsPerCurrency.rewardCount())
                                .amount(earningsPerCurrency.money().amount())
                                .dollarsEquivalent(prettyUsd(earningsPerCurrency.money().dollarsEquivalent()))
                                .currency(new ShortCurrencyResponse()
                                        .id(earningsPerCurrency.money().currency().id().value())
                                        .code(earningsPerCurrency.money().currency().code())
                                        .name(earningsPerCurrency.money().currency().name())
                                        .decimals(earningsPerCurrency.money().currency().decimals())
                                        .logoUrl(earningsPerCurrency.money().currency().logoUrl())))
                        .toList()));
    }
}
