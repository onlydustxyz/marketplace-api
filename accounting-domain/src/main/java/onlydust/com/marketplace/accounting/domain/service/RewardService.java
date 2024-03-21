package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class RewardService implements AccountingRewardPort {

    // TODO X: remove
    private static final List<onlydust.com.marketplace.accounting.domain.model.Currency.Code> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT = List.of(
            onlydust.com.marketplace.accounting.domain.model.Currency.Code.STRK,
            onlydust.com.marketplace.accounting.domain.model.Currency.Code.USDC,
            Currency.Code.LORDS
    );

    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final MailNotificationPort mailNotificationPort;


    @Override
    public List<BackofficeRewardView> findByInvoiceId(Invoice.Id invoiceId) {
        return accountingRewardStoragePort.getInvoiceRewards(invoiceId);
    }

    @Override
    public Page<BackofficeRewardView> getRewards(int pageIndex, int pageSize,
                                                 List<RewardStatus> statuses,
                                                 Date fromRequestedAt, Date toRequestedAt,
                                                 Date fromProcessedAt, Date toProcessedAt) {
        final Set<RewardStatus> sanitizedStatuses = isNull(statuses) ? Set.of() : statuses.stream().collect(Collectors.toUnmodifiableSet());
        return accountingRewardStoragePort.findRewards(pageIndex, pageSize, sanitizedStatuses, fromRequestedAt, toRequestedAt, fromProcessedAt, toProcessedAt);
    }

    @Override
    public List<BackofficeRewardView> searchRewardsByInvoiceIds(List<Invoice.Id> invoiceIds) {
        return accountingRewardStoragePort.searchRewards(List.of(Invoice.Status.APPROVED), invoiceIds)
                .stream()
                .filter(rewardView -> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT.contains(rewardView.money().currency().code()))
                // TODO X: use reward status instead of this filter (could be added to the SQL query)
                .filter(rewardView -> isNull(rewardView.processedAt()) && rewardView.transactionReferences().isEmpty())
                .toList();
    }

    @Override
    public String exportRewardsCSV(List<RewardStatus> statuses,
                                   Date fromRequestedAt, Date toRequestedAt,
                                   Date fromProcessedAt, Date toProcessedAt) {
        final var rewards = accountingRewardStoragePort.findRewards(0, 1_000_000,
                statuses.stream().collect(Collectors.toUnmodifiableSet()), fromRequestedAt, toRequestedAt, fromProcessedAt, toProcessedAt);

        if (rewards.getTotalPageNumber() > 1) {
            throw badRequest("Too many rewards to export");
        }

        return RewardsExporter.csv(rewards.getContent());
    }

    @Override
    public void notifyAllNewPaidRewards() {
        final List<BackofficeRewardView> rewardViews = accountingRewardStoragePort.findPaidRewardsToNotify();
        for (Map.Entry<String, List<BackofficeRewardView>> listOfPaidRewardsMapToAdminEmail :
                rewardViews.stream().collect(groupingBy(rewardView -> rewardView.billingProfileAdmin().admins().get(0).email())).entrySet()) {
            mailNotificationPort.sendRewardsPaidMail(listOfPaidRewardsMapToAdminEmail.getKey(), listOfPaidRewardsMapToAdminEmail.getValue());
        }
        accountingRewardStoragePort.markRewardsAsPaymentNotified(rewardViews.stream()
                .map(BackofficeRewardView::id)
                .toList());
    }
}
