package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.in.BatchPaymentPort;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BatchPaymentMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SearchRewardMapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeAccountingManagement"))
@AllArgsConstructor
public class BackofficeAccountingManagementRestApi implements BackofficeAccountingManagementApi {
    private final AccountingFacadePort accountingFacadePort;
    private final RewardFacadePort rewardFacadePort;
    private final CurrencyFacadePort currencyFacadePort;
    private final UserFacadePort userFacadePort;
    private final AccountingRewardPort accountingRewardPort;
    private final BatchPaymentPort batchPaymentPort;

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
        final var page = accountingFacadePort.transactionHistory(SponsorId.of(sponsorId), sanitizedPageIndex, sanitizePageSize(pageSize));
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
    public ResponseEntity<PendingPaymentListResponse> getPendingPayments() {
        final var payableRewards = accountingFacadePort.getPayableRewards();

        return ResponseEntity.ok(new PendingPaymentListResponse()
                .payments(payableRewards.stream().map(BackOfficeMapper::mapPendingPaymentToResponse).toList())
        );
    }

    @Override
    public ResponseEntity<Void> payReward(UUID rewardId, PayRewardRequest payRewardRequest) {
        final var reward = rewardFacadePort.getReward(rewardId)
                .orElseThrow(() -> notFound("Reward %s not found".formatted(rewardId)));

        final var currency =
                currencyFacadePort.listCurrencies().stream().filter(c -> c.code().toString().equals(reward.currency().toString().toUpperCase())).findFirst()
                        .orElseThrow(() -> notFound("Currency %s not found".formatted(reward.currency().toString().toUpperCase())));

        final var recipient = userFacadePort.getProfileById(reward.recipientId());

        final var paymentReference = new SponsorAccount.PaymentReference(mapTransactionNetwork(payRewardRequest.getNetwork()),
                payRewardRequest.getReference(),
                recipient.getLogin(),
                payRewardRequest.getRecipientAccount());

        accountingFacadePort.pay(RewardId.of(rewardId), currency.id(), paymentReference);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RewardPageResponse> getRewards(Integer pageIndex, Integer pageSize,
                                                         List<RewardStatusContract> statuses,
                                                         String fromRequestedAt,
                                                         String toRequestedAt,
                                                         String fromProcessedAt,
                                                         String toProcessedAt) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final Page<BackofficeRewardView> rewards = accountingRewardPort.getRewards(
                sanitizedPageIndex,
                sanitizedPageSize,
                statuses != null ? statuses.stream().map(BackOfficeMapper::map).toList() : null,
                DateMapper.parseNullable(fromRequestedAt),
                DateMapper.parseNullable(toRequestedAt),
                DateMapper.parseNullable(fromProcessedAt),
                DateMapper.parseNullable(toProcessedAt)
        );
        return ResponseEntity.ok(SearchRewardMapper.rewardPageToResponse(sanitizedPageIndex, rewards));
    }

    @Override
    public ResponseEntity<String> exportRewardsCSV(List<RewardStatusContract> statuses,
                                                   String fromRequestedAt,
                                                   String toRequestedAt,
                                                   String fromProcessedAt,
                                                   String toProcessedAt) {
        if (statuses == null || statuses.isEmpty())
            throw badRequest("At least one status must be set in the filter");
        if (fromRequestedAt == null && toRequestedAt == null && fromProcessedAt == null && toProcessedAt == null)
            throw badRequest("At least one of the date filters must be set");

        final String csv = accountingRewardPort.exportRewardsCSV(
                statuses.stream().map(BackOfficeMapper::map).toList(),
                DateMapper.parseNullable(fromRequestedAt),
                DateMapper.parseNullable(toRequestedAt),
                DateMapper.parseNullable(fromProcessedAt),
                DateMapper.parseNullable(toProcessedAt)
        );
        return ResponseEntity.ok(csv);
    }

    @Override
    public ResponseEntity<SearchRewardsResponse> searchRewards(SearchRewardsRequest searchRewardsRequest) {
        final var invoiceIds = searchRewardsRequest.getInvoiceIds() != null ?
                searchRewardsRequest.getInvoiceIds().stream().map(Invoice.Id::of).toList() : null;
        final List<BackofficeRewardView> rewardViews = accountingRewardPort.searchRewardsByInvoiceIds(invoiceIds);
        return ResponseEntity.ok(SearchRewardMapper.searchRewardToResponse(rewardViews));
    }

    @Override
    public ResponseEntity<BatchPaymentsResponse> createBatchPayments(PostBatchPaymentRequest postBatchPaymentRequest) {
        final var batchPayments =
                batchPaymentPort.createBatchPaymentsForInvoices(postBatchPaymentRequest.getInvoiceIds().stream().map(Invoice.Id::of).toList());
        return ResponseEntity.ok(BatchPaymentMapper.domainToResponse(batchPayments));
    }

    @Override
    public ResponseEntity<Void> updateBatchPayment(UUID batchPaymentId, BatchPaymentRequest batchPaymentRequest) {
        batchPaymentPort.markBatchPaymentAsPaid(BatchPayment.Id.of(batchPaymentId), batchPaymentRequest.getTransactionHash());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BatchPaymentDetailsResponse> getBatchPayment(UUID batchPaymentId) {
        return ResponseEntity.ok(BatchPaymentMapper.domainToDetailedResponse(batchPaymentPort.findBatchPaymentById(BatchPayment.Id.of(batchPaymentId))));
    }

    @Override
    public ResponseEntity<BatchPaymentPageResponse> getBatchPayments(Integer pageIndex, Integer pageSize, List<BatchPaymentStatus> statuses) {
        final int sanitizePageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final int sanitizedPageSize = PaginationHelper.sanitizePageSize(pageSize);
        final BatchPaymentPageResponse batchPaymentPageResponse = BatchPaymentMapper.pageToResponse(
                batchPaymentPort.findBatchPayments(sanitizePageIndex, sanitizedPageSize, statuses == null ? null :
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
}
