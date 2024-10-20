package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.events.TrackingRewardCreated;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class AccountingTrackingNotifier implements AccountingObserverPort {

    private final OutboxPort trackingOutbox;
    private final AccountingRewardStoragePort accountingRewardStoragePort;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {

    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {

    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        final var rewardDetailsView = accountingRewardStoragePort.getReward(rewardId)
                .orElseThrow(() -> internalServerError(("Reward %s not found").formatted(rewardId.value())));
        trackingOutbox.push(new TrackingRewardCreated(
                rewardDetailsView.project().id(),
                rewardDetailsView.recipient().githubUserId().value(),
                isNull(rewardDetailsView.recipient().userId()) ? null : rewardDetailsView.recipient().userId().value(),
                rewardDetailsView.requester().githubUserId().value(),
                rewardDetailsView.requester().userId().value(),
                rewardDetailsView.money().currency().code().toString(),
                rewardDetailsView.money().amount(),
                rewardDetailsView.money().dollarsEquivalent().orElse(null),
                rewardDetailsView.githubUrls().size(),
                rewardId
        ));
    }

    @Override
    public void onRewardCancelledBefore(RewardId rewardId) {
    }

    @Override
    public void onRewardCancelledAfter(RewardId rewardId) {
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {

    }

    @Override
    public void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {

    }

    @Override
    public void onBillingProfileEnableChanged(BillingProfile.Id billingProfileId, Boolean enabled) {

    }

    @Override
    public void onBillingProfileDeleted(BillingProfile.Id billingProfileId) {

    }

    @Override
    public void onFundsAllocatedToProgram(SponsorId from, ProgramId to, PositiveAmount amount, Currency.Id currencyId) {

    }

    @Override
    public void onFundsRefundedByProgram(ProgramId from, SponsorId to, PositiveAmount amount, Currency.Id currencyId) {

    }

    @Override
    public void onFundsGrantedToProject(ProgramId from, ProjectId to, PositiveAmount amount, Currency.Id currencyId) {

    }

    @Override
    public void onFundsRefundedByProject(ProjectId from, ProgramId to, PositiveAmount amount, Currency.Id currencyId) {

    }
}
