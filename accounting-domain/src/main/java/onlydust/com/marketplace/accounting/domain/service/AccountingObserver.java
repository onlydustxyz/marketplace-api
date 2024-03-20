package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class AccountingObserver implements AccountingObserverPort, RewardStatusFacadePort, BillingProfileObserver {
    // TODO migrate rewards to accounting schema and merge all those storages as onetone dependencies of reward
    private final RewardStatusStorage rewardStatusStorage;
    private final RewardUsdEquivalentStorage rewardUsdEquivalentStorage;
    private final QuoteStorage quoteStorage;
    private final CurrencyStorage currencyStorage;
    private final InvoiceStoragePort invoiceStorage;
    private final ReceiptStoragePort receiptStorage;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {
        refreshRelatedRewardsStatuses(sponsorAccount);
    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {
        refreshRelatedRewardsStatuses(sponsorAccount);
    }

    private void refreshRelatedRewardsStatuses(SponsorAccountStatement sponsorAccount) {
        sponsorAccount.awaitingPayments().forEach((rewardId, amount) -> {
            final var rewardStatus = rewardStatusStorage.get(rewardId)
                    .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(rewardId)));
            rewardStatusStorage.save(upToDateRewardStatus(sponsorAccount.accountBookFacade(), rewardStatus));
        });
    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        rewardStatusStorage.save(upToDateRewardStatus(accountBookFacade, new RewardStatusData(rewardId)));
        updateUsdEquivalent(rewardId);
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
        rewardStatusStorage.delete(rewardId);
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        final var rewardStatus = rewardStatusStorage.get(rewardId)
                .orElseThrow(() -> internalServerError("RewardStatus not found for reward %s".formatted(rewardId)));
        rewardStatusStorage.save(rewardStatus.paidAt(ZonedDateTime.now()));

        invoiceStorage.invoiceOf(rewardId).ifPresent(invoice -> {
            if (invoice.rewards().stream().allMatch(reward -> rewardStatusStorage.get(reward.id()).map(RewardStatusData::isPaid)
                    .orElseThrow(() -> internalServerError("RewardStatus not found for reward %s".formatted(rewardId))))) {
                invoiceStorage.update(invoice.status(Invoice.Status.PAID));
            }
        });
    }

    @Override
    public void onPaymentReceived(RewardId rewardId, BatchPayment.Reference reference) {
        receiptStorage.save(Receipt.of(rewardId, reference));
    }

    public void updateUsdEquivalent(RewardId rewardId) {
        final var rewardStatus = rewardStatusStorage.get(rewardId)
                .orElseThrow(() -> internalServerError("RewardStatus not found for reward %s".formatted(rewardId)));
        usdAmountOf(rewardId).ifPresentOrElse(
                usdAmount -> rewardStatusStorage.save(rewardStatus.usdAmount(usdAmount)),
                () -> rewardStatusStorage.save(rewardStatus.usdAmount(null))
        );
    }

    @Override
    public Optional<ConvertedAmount> usdAmountOf(RewardId rewardId) {
        final var usd = currencyStorage.findByCode(Currency.Code.USD)
                .orElseThrow(() -> internalServerError("Currency USD not found"));

        return rewardUsdEquivalentStorage.get(rewardId).flatMap(
                rewardUsdEquivalent -> rewardUsdEquivalent.equivalenceSealingDate()
                        .flatMap(date -> quoteStorage.nearest(rewardUsdEquivalent.rewardCurrencyId(), usd.id(), date))
                        .map(quote -> new ConvertedAmount(Amount.of(quote.convertToBaseCurrency(rewardUsdEquivalent.rewardAmount())),
                                quote.price())));
    }

    private RewardStatusData upToDateRewardStatus(AccountBookFacade accountBookFacade, RewardStatusData rewardStatusData) {
        return rewardStatusData
                .sponsorHasEnoughFund(accountBookFacade.isFunded(rewardStatusData.rewardId()))
                .unlockDate(accountBookFacade.unlockDateOf(rewardStatusData.rewardId()).map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .withAdditionalNetworks(accountBookFacade.networksOf(rewardStatusData.rewardId()))
                .usdAmount(usdAmountOf(rewardStatusData.rewardId()).orElse(null));
    }


    @Override
    public void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal) {
        final var invoice = invoiceStorage.get(invoiceId).orElseThrow(() -> notFound("Invoice %s not found".formatted(invoiceId)));
        invoice.rewards().forEach(reward -> {
            final var rewardStatus = rewardStatusStorage.get(reward.id())
                    .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(reward.id())));
            rewardStatusStorage.save(rewardStatus.invoiceReceivedAt(invoice.createdAt()));
        });
    }

    @Override
    public void onInvoiceRejected(@NonNull InvoiceRejected invoiceRejected) {
        invoiceRejected.rewards().forEach(reward -> {
            final var rewardStatus = rewardStatusStorage.get(reward.id())
                    .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(reward.id())));
            rewardStatusStorage.save(rewardStatus.invoiceReceivedAt(null));
        });
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        // TODO ?
    }

    @Override
    public void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId, @NonNull UserId userId, @NonNull ProjectId projectId) {
        refreshRewardsUsdEquivalentOf(billingProfileId);
        rewardStatusStorage.updateBillingProfileForRecipientUserIdAndProjectId(billingProfileId, userId, projectId);
    }

    @Override
    public void onBillingProfileEnabled(BillingProfile.Id billingProfileId, Boolean enabled) {
        if (enabled) {
            rewardStatusStorage.enableBillingProfile(billingProfileId);
        } else {
            rewardStatusStorage.disabledBillingProfile(billingProfileId);
        }
    }

    private void refreshRewardsUsdEquivalentOf(BillingProfile.Id billingProfileId) {
        rewardStatusStorage.notPaid(billingProfileId).forEach(rewardStatus ->
                rewardStatusStorage.save(rewardStatus.usdAmount(usdAmountOf(rewardStatus.rewardId()).orElse(null)))
        );
    }

    public void refreshRewardsUsdEquivalents() {
        rewardStatusStorage.notPaid().forEach(rewardStatus ->
                rewardStatusStorage.save(rewardStatus.usdAmount(usdAmountOf(rewardStatus.rewardId()).orElse(null)))
        );
    }
}
