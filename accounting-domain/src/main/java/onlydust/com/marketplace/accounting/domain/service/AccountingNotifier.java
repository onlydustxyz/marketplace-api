package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.notification.BillingProfileVerificationFailed;
import onlydust.com.marketplace.accounting.domain.notification.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Slf4j
@AllArgsConstructor
public class AccountingNotifier implements AccountingObserverPort, BillingProfileObserverPort {
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final InvoiceStoragePort invoiceStoragePort;
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
        final var reward = accountingRewardStoragePort.getReward(rewardId)
                .orElseThrow(() -> internalServerError("Reward %s not found".formatted(rewardId)));

        if (nonNull(reward.recipient().email())) {
            notificationPort.push(reward.recipient().userId().value(), RewardCanceled.builder()
                    .shortReward(ShortReward.builder()
                            .amount(reward.money().amount())
                            .currencyCode(reward.money().currency().code().toString())
                            .dollarsEquivalent(reward.money().getDollarsEquivalentValue())
                            .id(rewardId)
                            .projectName(reward.project().name())
                            .build())
                    .build());
        } else {
            LOGGER.warn("Unable to send cancel reward mail to recipient %s due to missing email".formatted(reward.recipient().login()));
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

        notificationPort.push(billingProfileAdmin.userId().value(), InvoiceRejected.builder()
                .billingProfileId(invoice.billingProfileSnapshot().id().value())
                .invoiceName(invoice.number().value())
                .rejectionReason(rejectionReason)
                .rewards(invoice.rewards().stream()
                        .map(reward -> ShortReward.builder()
                                .id(reward.id())
                                .amount(reward.amount().getValue())
                                .currencyCode(reward.amount().getCurrency().code().toString())
                                .projectName(reward.projectName())
                                .dollarsEquivalent(reward.target().getValue())
                                .build()
                        ).toList())
                .build());
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated event) {
        if (event.failed()) {
            final var owner = billingProfileStoragePort.getBillingProfileOwnerById(event.getUserId())
                    .orElseThrow(() -> internalServerError(("Owner %s not found for billing profile %s")
                            .formatted(event.getUserId().value(), event.getBillingProfileId().value())));

            if (nonNull(owner.email())) {
                notificationPort.push(owner.userId().value(), BillingProfileVerificationFailed.builder()
                        .billingProfileId(event.getBillingProfileId())
                        .verificationStatus(event.getVerificationStatus())
                        .build());
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
