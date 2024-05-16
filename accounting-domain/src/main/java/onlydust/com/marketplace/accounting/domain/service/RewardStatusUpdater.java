package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;

import java.time.ZonedDateTime;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Slf4j
@AllArgsConstructor
public class RewardStatusUpdater implements AccountingObserverPort, BillingProfileObserverPort {
    // TODO migrate rewards to accounting schema and merge all those storages as onetone dependencies of reward
    private final RewardStatusFacadePort rewardStatusFacadePort;
    private final RewardStatusStorage rewardStatusStorage;
    private final InvoiceStoragePort invoiceStorage;
    private final AccountingRewardStoragePort accountingRewardStoragePort;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {
        rewardStatusFacadePort.refreshRelatedRewardsStatuses(sponsorAccount);
    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {
        rewardStatusFacadePort.refreshRelatedRewardsStatuses(sponsorAccount);
    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        accountingRewardStoragePort.updateBillingProfileFromRecipientPayoutPreferences(rewardId);
        rewardStatusFacadePort.refreshRewardsUsdEquivalentOf(rewardId);
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        rewardStatusStorage.save(mustGetRewardStatus(rewardId).paidAt(ZonedDateTime.now()));

        invoiceStorage.invoiceOf(rewardId).ifPresent(invoice -> {
            if (invoice.rewards().stream().allMatch(reward -> mustGetRewardStatus(reward.id()).isPaid())) {
                invoiceStorage.update(invoice.status(Invoice.Status.PAID));
            }
        });
    }

    @Override
    public void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal) {
        final var invoice = invoiceStorage.get(invoiceId).orElseThrow(() -> notFound("Invoice %s not found".formatted(invoiceId)));
        invoice.rewards().forEach(reward -> {
            rewardStatusStorage.save(mustGetRewardStatus(reward.id()).invoiceReceivedAt(invoice.createdAt()));
        });
    }

    @Override
    public void onInvoiceRejected(final @NonNull Invoice.Id invoiceId, final @NonNull String rejectionReason) {
        final var invoice = invoiceStorage.get(invoiceId)
                .orElseThrow(() -> internalServerError("Invoice %s not found".formatted(invoiceId)));
        invoice.rewards().forEach(reward -> rewardStatusStorage.save(mustGetRewardStatus(reward.id()).invoiceReceivedAt(null)));
    }

    private RewardStatusData mustGetRewardStatus(RewardId rewardId) {
        return rewardStatusStorage.get(rewardId)
                .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(rewardId.value())));
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated event) {
        rewardStatusFacadePort.refreshRewardsUsdEquivalentOf(event.getBillingProfileId());
    }

    @Override
    public void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId, @NonNull UserId userId, @NonNull ProjectId projectId) {
        accountingRewardStoragePort.updateBillingProfileForRecipientUserIdAndProjectId(billingProfileId, userId, projectId);
        rewardStatusFacadePort.refreshRewardsUsdEquivalentOf(billingProfileId);
    }

    @Override
    public void onBillingProfileEnableChanged(BillingProfile.Id billingProfileId, Boolean enabled) {
        if (!enabled) {
            final var updatedRewardIds = accountingRewardStoragePort.removeBillingProfile(billingProfileId);
            rewardStatusFacadePort.refreshRewardsUsdEquivalentOf(updatedRewardIds);
        }
    }

    @Override
    public void onBillingProfileDeleted(BillingProfile.Id billingProfileId) {
        final var updatedRewardIds = accountingRewardStoragePort.removeBillingProfile(billingProfileId);
        rewardStatusFacadePort.refreshRewardsUsdEquivalentOf(updatedRewardIds);
    }
}
