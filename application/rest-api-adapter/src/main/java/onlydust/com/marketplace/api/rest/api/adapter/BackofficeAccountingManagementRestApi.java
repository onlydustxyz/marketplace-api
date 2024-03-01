package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SearchRewardMapper;
import onlydust.com.marketplace.project.domain.model.OldPayRewardRequestCommand;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@RestController
@Tags(@Tag(name = "BackofficeAccountingManagement"))
@AllArgsConstructor
public class BackofficeAccountingManagementRestApi implements BackofficeAccountingManagementApi {
    private final AccountingFacadePort accountingFacadePort;
    private final RewardFacadePort rewardFacadePort;
    private final CurrencyFacadePort currencyFacadePort;
    private final UserFacadePort userFacadePort;
    private final RewardFacadePort rewardFacadePortV2;
    private final AccountingRewardPort accountingRewardPort;

    @Override
    public ResponseEntity<AccountResponse> createSponsorAccount(UUID sponsorUuid, CreateAccountRequest createAccountRequest) {
        final var sponsorId = SponsorId.of(sponsorUuid);
        final var currencyId = Currency.Id.of(createAccountRequest.getCurrencyId());
        final var allowance = PositiveAmount.of(createAccountRequest.getAllowance());
        final var lockedUntil = createAccountRequest.getLockedUntil();
        final var transaction = createAccountRequest.getReceipt() == null ? null : mapReceiptToTransaction(createAccountRequest.getReceipt());

        final var sponsorAccountStatement = transaction == null ?
                accountingFacadePort.createSponsorAccount(sponsorId, currencyId, allowance, lockedUntil) :
                accountingFacadePort.createSponsorAccount(sponsorId, currencyId, allowance, lockedUntil, transaction);

        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountListResponse> getSponsorAccounts(UUID sponsorId) {
        final var sponsorAccounts = accountingFacadePort.getSponsorAccounts(SponsorId.of(sponsorId));
        return ResponseEntity.ok(new AccountListResponse().accounts(sponsorAccounts.stream().map(BackOfficeMapper::mapAccountToResponse).toList()));
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
    public ResponseEntity<Void> oldPayReward(UUID rewardId, PayRewardRequest payRewardRequest) {
        rewardFacadePortV2.oldPayReward(OldPayRewardRequestCommand.builder()
                .rewardId(rewardId)
                .recipientAccount(payRewardRequest.getRecipientAccount())
                .transactionReference(payRewardRequest.getReference())
                .currency(switch (payRewardRequest.getNetwork()) {
                    case SWIFT -> onlydust.com.marketplace.project.domain.model.Currency.USD;
                    case SEPA -> onlydust.com.marketplace.project.domain.model.Currency.USD;
                    case ETHEREUM -> onlydust.com.marketplace.project.domain.model.Currency.ETH;
                    case OPTIMISM -> onlydust.com.marketplace.project.domain.model.Currency.OP;
                    case STARKNET -> onlydust.com.marketplace.project.domain.model.Currency.STRK;
                    case APTOS -> onlydust.com.marketplace.project.domain.model.Currency.APT;
                })
                .build());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<SearchRewardsResponse> searchRewards(SearchRewardsRequest searchRewardsRequest) {
        final var invoiceIds = searchRewardsRequest.getInvoiceIds() != null ?
                searchRewardsRequest.getInvoiceIds().stream().map(Invoice.Id::of).toList() : null;
        final List<RewardView> rewardViews = accountingRewardPort.searchForApprovedInvoiceIds(invoiceIds);
        return ResponseEntity.ok(SearchRewardMapper.searchRewardToResponse(rewardViews));
    }
}
