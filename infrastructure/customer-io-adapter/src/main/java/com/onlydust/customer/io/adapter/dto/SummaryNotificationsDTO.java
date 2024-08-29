package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationToReview;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationRefused;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.*;

public record SummaryNotificationsDTO(@NonNull String title,
                                      @NonNull String username,
                                      @NonNull String description,
                                      @NonNull List<SummaryNotificationDTO> notifications) {

    private static final String TITLE = "Weekly notifications";
    private static final String DESCRIPTION = "Here is a summary of your notifications from last week. Please review them at your convenience.";

    public static SummaryNotificationsDTO from(@NonNull NotificationRecipient recipient, @NonNull List<SendableNotification> sendableNotifications,
                                               @NonNull String environment) {
        return new SummaryNotificationsDTO(TITLE, recipient.login(), DESCRIPTION, sendableNotifications.stream()
                .map((SendableNotification sendableNotification) -> SummaryNotificationDTO.from(sendableNotification, environment))
                .toList());
    }

    public record SummaryNotificationDTO(@NonNull String title,
                                         @NonNull String description,
                                         ButtonDTO button) {

        public static SummaryNotificationDTO from(@NonNull SendableNotification sendableNotification, @NonNull String environment) {
            if (sendableNotification.data() instanceof BillingProfileVerificationClosed billingProfileVerificationClosed) {
                return new SummaryNotificationDTO(
                        "Your billing profile has been closed",
                        "Your billing profile %s has been closed, please contact support for more information"
                                .formatted(billingProfileVerificationClosed.billingProfileName()),
                        new ButtonDTO("Contact us", getMarketplaceBillingProfileUrlFromEnvironment(environment,
                                billingProfileVerificationClosed.billingProfileId().value()))
                );
            } else if (sendableNotification.data() instanceof CommitteeApplicationCreated committeeApplicationCreated) {
                return new SummaryNotificationDTO(
                        "New committee application",
                        "You have applied to %s committee.".formatted(committeeApplicationCreated.getCommitteeName()),
                        new ButtonDTO("Review my answer",
                                getMarketplaceCommitteeApplicationUrlFromEnvironment(environment, committeeApplicationCreated.getCommitteeId(),
                                        committeeApplicationCreated.getProjectId()))
                );
            } else if (sendableNotification.data() instanceof RewardReceived rewardReceived) {
                final RewardDTO rewardDTO = RewardDTO.from(rewardReceived.shortReward());
                return new SummaryNotificationDTO(
                        "You have received a new reward",
                        "%s sent you a new reward of %s %s on project %s"
                                .formatted(rewardDTO.sentBy(),
                                        rewardDTO.amount(),
                                        rewardDTO.currency(),
                                        rewardDTO.projectName()),
                        new ButtonDTO("See details", getMarketplaceMyRewardsUrlFromEnvironment(environment))

                );
            } else if (sendableNotification.data() instanceof RewardCanceled rewardCanceled) {
                final RewardDTO rewardDTO = RewardDTO.from(rewardCanceled.shortReward());
                return new SummaryNotificationDTO(
                        "Your reward has been canceled",
                        "Your reward of %s %s has been canceled for the project %s"
                                .formatted(rewardDTO.amount(), rewardDTO.currency(), rewardDTO.projectName()),
                        null
                );
            } else if (sendableNotification.data() instanceof RewardsPaid rewardsPaid) {
                return new SummaryNotificationDTO(
                        "Your rewards has been paid",
                        "%s reward(s) has been paid for a total of %s USD"
                                .formatted(rewardsPaid.shortRewards().size(), rewardsPaid.shortRewards().stream()
                                        .map(ShortReward::getDollarsEquivalent)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .setScale(3, RoundingMode.HALF_UP)
                                        .toString()),
                        new ButtonDTO("See details", getMarketplaceMyRewardsUrlFromEnvironment(environment))
                );
            } else if (sendableNotification.data() instanceof InvoiceRejected invoiceRejected) {
                return new SummaryNotificationDTO(
                        "Your invoice has been rejected",
                        "Your invoice %s has been rejected because of : %s"
                                .formatted(invoiceRejected.invoiceName(), invoiceRejected.rejectionReason()),
                        new ButtonDTO("Upload another invoice", getMarketplaceFrontendUrlFromEnvironment(environment) + "rewards")
                );
            } else if (sendableNotification.data() instanceof ApplicationAccepted applicationAccepted) {
                return new SummaryNotificationDTO(
                        "Your application has been accepted",
                        "Your application for %s has been accepted".formatted(applicationAccepted.getIssue().title()),
                        new ButtonDTO("See my applications", getMarketplaceMyApplicationsFromEnvironment(environment))
                );
            } else if (sendableNotification.data() instanceof ApplicationRefused applicationRefused) {
                return new SummaryNotificationDTO(
                        "Your application has been refused",
                        "Your application for %s has been refused".formatted(applicationRefused.getIssue().title()),
                        new ButtonDTO("See my applications", getMarketplaceMyApplicationsFromEnvironment(environment))
                );
            } else if (sendableNotification.data() instanceof CompleteYourBillingProfile completeYourBillingProfile) {
                return new SummaryNotificationDTO(
                        "Your billing profile is incomplete",
                        "Your billing profile %s is incomplete, please update it to complete the process"
                                .formatted(completeYourBillingProfile.billingProfile().billingProfileName()),
                        new ButtonDTO("Resume my billing profile", getMarketplaceBillingProfileUrlFromEnvironment(environment,
                                completeYourBillingProfile.billingProfile().billingProfileId()))

                );
            } else if (sendableNotification.data() instanceof BillingProfileVerificationRejected billingProfileVerificationRejected) {
                return new SummaryNotificationDTO(
                        "Your billing profile has been rejected",
                        "Your billing profile %s has been rejected because of : %s"
                                .formatted(billingProfileVerificationRejected.billingProfileName(), billingProfileVerificationRejected.rejectionReason()),
                        new ButtonDTO("Resume verification", getMarketplaceBillingProfileUrlFromEnvironment(environment,
                                billingProfileVerificationRejected.billingProfileId().value()))
                );
            } else if (sendableNotification.data() instanceof BillingProfileVerificationClosed billingProfileVerificationClosed) {
                return new SummaryNotificationDTO(
                        "Your billing profile has been closed",
                        "Your billing profile %s has been closed, please contact support for more information"
                                .formatted(billingProfileVerificationClosed.billingProfileName()),
                        new ButtonDTO("Contact us", getMarketplaceBillingProfileUrlFromEnvironment(environment,
                                billingProfileVerificationClosed.billingProfileId().value()))
                );
            } else if (sendableNotification.data() instanceof ApplicationToReview applicationToReview) {
                return new SummaryNotificationDTO(
                        "New contributor application",
                        "We wanted to inform you that a contributor named %s has applied to work on your issue %s on project %s"
                                .formatted(applicationToReview.getUser().login(), applicationToReview.getIssue().title(),
                                        applicationToReview.getProject().name()),
                        new ButtonDTO("Review",
                                getMarketplaceFrontendUrlFromEnvironment(environment) + "p/%s/applications".formatted(applicationToReview.getProject().slug()))
                );
            }
            throw OnlyDustException.internalServerError("No summary email mapping found for sendableNotification %s".formatted(sendableNotification));
        }
    }
}
