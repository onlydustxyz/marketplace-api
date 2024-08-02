package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationFailed;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.events.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Slf4j
@AllArgsConstructor
public class AccountingNotifier implements AccountingObserverPort, BillingProfileObserverPort {
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final InvoiceStoragePort invoiceStoragePort;
    private final OutboxPort accountingOutbox;
    private final NotificationPort notificationPort;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {

    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {
    }


    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        final var reward = accountingRewardStoragePort.getReward(rewardId)
                .orElseThrow(() -> internalServerError(("Reward %s not found").formatted(rewardId.value())));
        if (nonNull(reward.recipient().email())) {
            notificationPort.push(reward.recipient().userId().value(), RewardReceived.builder()
                    .contributionCount(reward.githubUrls().size())
                    .sentByGithubLogin(reward.requester().login())
                    .shortReward(ShortReward.builder()
                            .amount(reward.money().amount())
                            .currencyCode(reward.money().currency().code().toString())
                            .dollarsEquivalent(reward.money().getDollarsEquivalentValue())
                            .id(rewardId)
                            .projectName(reward.project().name())
                            .build())
                    .build());
        } else {
            LOGGER.warn("Unable to send reward created email to contributor %s".formatted(reward.recipient().login()));
        }
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
        final var shortRewardDetailsView = accountingRewardStoragePort.getShortReward(rewardId)
                .orElseThrow(() -> internalServerError("Reward %s not found".formatted(rewardId)));

        if (nonNull(shortRewardDetailsView.recipient().email())) {
            accountingOutbox.push(new RewardCanceled(
                    shortRewardDetailsView.recipient().email(),
                    shortRewardDetailsView.recipient().login(),
                    ShortReward.builder()
                            .amount(shortRewardDetailsView.money().amount())
                            .currencyCode(shortRewardDetailsView.money().currency().code().toString())
                            .dollarsEquivalent(shortRewardDetailsView.money().getDollarsEquivalentValue())
                            .id(rewardId)
                            .projectName(shortRewardDetailsView.project().name())
                            .build(),
                    isNull(shortRewardDetailsView.recipient().userId()) ? null : shortRewardDetailsView.recipient().userId().value()));
        } else {
            LOGGER.warn("Unable to send cancel reward mail to recipient %s due to missing email".formatted(shortRewardDetailsView.recipient().login()));
        }
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
    }

    @Override
    public void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal) {
    }

    @Override
    public void onInvoiceRejected(final @NonNull Invoice.Id id, final @NonNull String rejectionReason) {
        final var invoice = invoiceStoragePort.get(id).orElseThrow(() -> OnlyDustException.notFound("Invoice %s not found".formatted(id)));

        final var billingProfileAdmin = billingProfileStoragePort.findBillingProfileAdmin(invoice.createdBy(), invoice.billingProfileSnapshot().id())
                .orElseThrow(() -> notFound("Billing profile admin not found for billing profile %s".formatted(invoice.billingProfileSnapshot().id())));

        accountingOutbox.push(new InvoiceRejected(billingProfileAdmin.email(),
                (long) invoice.rewards().size(), billingProfileAdmin.login(),
                billingProfileAdmin.firstName(),
                billingProfileAdmin.userId().value(),
                invoice.number().value(),
                invoice.rewards().stream()
                        .map(reward -> ShortReward.builder()
                                .id(reward.id())
                                .amount(reward.amount().getValue())
                                .currencyCode(reward.amount().getCurrency().code().toString())
                                .projectName(reward.projectName())
                                .dollarsEquivalent(reward.target().getValue())
                                .build()
                        ).toList(),
                rejectionReason));
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated event) {
        if (event.failed()) {
            final ShortContributorView owner = billingProfileStoragePort.getBillingProfileOwnerById(event.getUserId())
                    .orElseThrow(() -> internalServerError(("Owner %s not found for billing profile %s")
                            .formatted(event.getUserId().value(), event.getBillingProfileId().value())));
            if (nonNull(owner.email())) {
                accountingOutbox.push(new BillingProfileVerificationFailed(owner.email(), owner.userId(), event.getBillingProfileId(), owner.login(),
                        event.getVerificationStatus()));
            } else {
                LOGGER.warn("Unable to send billing profile verifcation failed mail to user %s".formatted(event.getUserId()));
            }
        }
    }

    @Override
    public void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId, @NonNull UserId userId, @NonNull ProjectId projectId) {
    }

    @Override
    public void onBillingProfileEnableChanged(BillingProfile.Id billingProfileId, Boolean enabled) {
    }

    @Override
    public void onBillingProfileDeleted(BillingProfile.Id billingProfileId) {
    }
}
