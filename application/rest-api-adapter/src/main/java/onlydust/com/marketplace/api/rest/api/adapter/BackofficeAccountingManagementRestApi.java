package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PaymentPort;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedBackofficeUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BatchPaymentMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.map;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapTransactionNetwork;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "BackofficeAccountingManagement"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeAccountingManagementRestApi implements BackofficeAccountingManagementApi {
    private final AccountingFacadePort accountingFacadePort;
    private final AccountingRewardPort accountingRewardPort;
    private final PaymentPort paymentPort;
    private final AuthenticatedBackofficeUserService authenticatedBackofficeUserService;
    private final BlockchainFacadePort blockchainFacadePort;

    @Override
    public ResponseEntity<Void> createSponsorAccount(UUID sponsorUuid, CreateAccountRequest createAccountRequest) {
        accountingFacadePort.createSponsorAccountWithInitialAllowance(SponsorId.of(sponsorUuid),
                Currency.Id.of(createAccountRequest.getCurrencyId()),
                null,
                PositiveAmount.of(createAccountRequest.getAllowance()));

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> unallocateBudgetFromProgram(UUID sponsorId, AllocationRequest request) {
        accountingFacadePort.unallocate(ProgramId.of(request.getProgramId()),
                SponsorId.of(sponsorId),
                PositiveAmount.of(request.getAmount()),
                Currency.Id.of(request.getCurrencyId()));

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> ungrantBudgetFromProject(UUID programId, GrantRequest request) {
        accountingFacadePort.ungrant(ProjectId.of(request.getProjectId()),
                ProgramId.of(programId),
                PositiveAmount.of(request.getAmount()),
                Currency.Id.of(request.getCurrencyId()));

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> payReward(UUID rewardId, PayRewardRequest payRewardRequest) {
        final var network = mapTransactionNetwork(payRewardRequest.getNetwork());
        final var transactionTimestamp = network.blockchain()
                .map(blockchain -> blockchainFacadePort.getTransaction(blockchain, payRewardRequest.getReference())
                        .map(Blockchain.Transaction::timestamp)
                        .orElseThrow(() -> badRequest("Transaction %s not found on blockchain %s"
                                .formatted(payRewardRequest.getReference(), blockchain.pretty()))))
                .orElse(ZonedDateTime.now());

        accountingFacadePort.pay(
                RewardId.of(rewardId),
                transactionTimestamp,
                network,
                payRewardRequest.getReference());

        return noContent().build();
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
        return ok(csv);
    }

    @Override
    public ResponseEntity<RewardDetailsResponse> getReward(UUID rewardId) {
        final var authenticatedUser = authenticatedBackofficeUserService.getAuthenticatedBackofficeUser().asAuthenticatedUser();
        final var reward = accountingRewardPort.getReward(RewardId.of(rewardId));
        return ok(map(reward, authenticatedUser));
    }

    @Override
    public ResponseEntity<BatchPaymentsResponse> createBatchPayments(PostBatchPaymentRequest postBatchPaymentRequest) {
        final var batchPayments = paymentPort.createPaymentsForInvoices(postBatchPaymentRequest.getInvoiceIds().stream().map(Invoice.Id::of).toList());
        return ok(BatchPaymentMapper.map(batchPayments));
    }

    @Override
    public ResponseEntity<Void> updateBatchPayment(UUID batchPaymentId, BatchPaymentRequest batchPaymentRequest) {
        paymentPort.markPaymentAsPaid(Payment.Id.of(batchPaymentId), batchPaymentRequest.getTransactionHash());
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteBatchPayment(UUID batchPaymentId) {
        paymentPort.deletePaymentById(Payment.Id.of(batchPaymentId));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> notifyRewardsPaid() {
        accountingRewardPort.notifyAllNewPaidRewards();
        return ok().build();
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
        return ok(new EarningsResponse()
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
