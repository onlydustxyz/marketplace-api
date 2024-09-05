package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;

import java.util.List;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Slf4j
@AllArgsConstructor
public class AccountingNotifier implements AccountingObserverPort, BillingProfileObserverPort, DepositObserverPort {
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final InvoiceStoragePort invoiceStoragePort;
    private final NotificationPort notificationPort;
    private final EmailStoragePort emailStoragePort;
    private final ProjectServicePort projectServicePort;
    private final DepositStoragePort depositStoragePort;

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
                            .contributionsCount(reward.githubUrls().size())
                            .sentByGithubLogin(reward.requester().login())
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
                            .sentByGithubLogin(reward.requester().login())
                            .contributionsCount(reward.githubUrls().size())
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

        final List<ShortReward> shortRewards = invoice.rewards().stream()
                .map(Invoice.Reward::id)
                .map(rewardId -> accountingRewardStoragePort.getReward(rewardId)
                        .orElseThrow(() -> internalServerError("Reward %s not found".formatted(rewardId))))
                .map(reward -> ShortReward.builder()
                        .id(reward.id())
                        .amount(reward.money().amount())
                        .currencyCode(reward.money().currency().code().toString())
                        .projectName(reward.project().name())
                        .contributionsCount(reward.githubUrls().size())
                        .sentByGithubLogin(reward.requester().login())
                        .dollarsEquivalent(reward.money().getDollarsEquivalentValue())
                        .build())
                .toList();

        notificationPort.push(billingProfileAdmin.userId().value(), InvoiceRejected.builder()
                .billingProfileId(invoice.billingProfileSnapshot().id().value())
                .invoiceName(invoice.number().value())
                .rejectionReason(rejectionReason)
                .rewards(shortRewards)
                .build());
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated event) {
        if (event.closed()) {
            sendBillingProfileVerificationClosedNotification(event);
        }
        if (event.rejected() && !event.isAChildrenKYC()) {
            sendBillingProfileRejectedNotification(event);
        }
    }

    private void sendBillingProfileRejectedNotification(BillingProfileVerificationUpdated event) {
        final var owner = billingProfileStoragePort.getBillingProfileOwnerById(event.getUserId())
                .orElseThrow(() -> internalServerError(("Owner %s not found for billing profile %s")
                        .formatted(event.getUserId().value(), event.getBillingProfileId().value())));

        final BillingProfile billingProfile = billingProfileStoragePort.findById(event.getBillingProfileId())
                .orElseThrow(() -> internalServerError("Billing profile %s not found".formatted(event.getBillingProfileId().value())));

        if (nonNull(owner.email())) {
            notificationPort.push(owner.userId().value(), BillingProfileVerificationRejected.builder()
                    .billingProfileId(event.getBillingProfileId())
                    .billingProfileName(billingProfile.name())
                    .rejectionReason(event.getReviewMessageForApplicant())
                    .build());
        } else {
            LOGGER.warn("Unable to send billing profile verification rejected mail to user %s".formatted(event.getUserId()));
        }
    }

    private void sendBillingProfileVerificationClosedNotification(BillingProfileVerificationUpdated event) {
        final var owner = billingProfileStoragePort.getBillingProfileOwnerById(event.getUserId())
                .orElseThrow(() -> internalServerError(("Owner %s not found for billing profile %s")
                        .formatted(event.getUserId().value(), event.getBillingProfileId().value())));

        final BillingProfile billingProfile = billingProfileStoragePort.findById(event.getBillingProfileId())
                .orElseThrow(() -> internalServerError("Billing profile %s not found".formatted(event.getBillingProfileId().value())));

        if (nonNull(owner.email())) {
            notificationPort.push(owner.userId().value(), BillingProfileVerificationClosed.builder()
                    .billingProfileId(event.getBillingProfileId())
                    .billingProfileName(billingProfile.name())
                    .build());
        } else {
            LOGGER.warn("Unable to send billing profile verification closed mail to user %s".formatted(event.getUserId()));
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

    @Override
    public void onFundsAllocatedToProgram(SponsorId sponsorId, ProgramId programId, PositiveAmount amount, Currency.Id currencyId) {
        projectServicePort.onFundsAllocatedToProgram(sponsorId, programId, amount, currencyId);
    }

    @Override
    public void onFundsRefundedByProgram(ProgramId programId, SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId) {
        projectServicePort.onFundsRefundedByProgram(programId, sponsorId, amount, currencyId);
    }

    @Override
    public void onBillingProfileExternalVerificationRequested(@NonNull BillingProfileChildrenKycVerification billingProfileChildrenKycVerification) {
        emailStoragePort.send(billingProfileChildrenKycVerification.individualKycIdentity().email(), billingProfileChildrenKycVerification);
    }

    @Override
    public void onDepositSubmittedByUser(UserId userId, Deposit.Id depositId) {

    }

    @Override
    public void onDepositRejected(Deposit.Id depositId) {
        final var deposit = depositStoragePort.find(depositId)
                .orElseThrow(() -> OnlyDustException.notFound("Deposit not found %s".formatted(depositId.value())));

        projectServicePort.onDepositRejected(deposit.id(), deposit.sponsorId(), deposit.transaction().amount(), deposit.currency().id(),
                deposit.transaction().timestamp());
    }

    @Override
    public void onDepositApproved(Deposit.Id depositId) {
        final var deposit = depositStoragePort.find(depositId)
                .orElseThrow(() -> OnlyDustException.notFound("Deposit not found %s".formatted(depositId.value())));

        projectServicePort.onDepositApproved(deposit.id(), deposit.sponsorId(), deposit.transaction().amount(), deposit.currency().id(),
                deposit.transaction().timestamp());
    }
}
