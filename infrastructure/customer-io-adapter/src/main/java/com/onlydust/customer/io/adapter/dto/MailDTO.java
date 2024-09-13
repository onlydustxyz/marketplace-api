package com.onlydust.customer.io.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationRefused;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.project.domain.model.notification.GoodFirstIssueCreated;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.List;

import static java.util.Objects.isNull;

@Builder
public record MailDTO<MessageData>(@NonNull @JsonProperty("transactional_message_id") String transactionalMessageId,
                                   @NonNull IdentifiersDTO identifiers,
                                   @NonNull String to,
                                   @NonNull String subject,
                                   @NonNull @JsonProperty("message_data") MessageData messageData
) {

    public MailDTO(@NonNull String transactionalMessageId, @NonNull IdentifiersDTO identifiers, @NonNull String to,
                   @NonNull String subject, @NonNull MessageData messageData) {
        this.transactionalMessageId = transactionalMessageId;
        this.identifiers = identifiers;
        this.to = to;
        this.subject = subject;
        this.messageData = messageData;
    }

    public static MailDTO<SummaryNotificationsDTO> from(CustomerIOProperties customerIOProperties, NotificationRecipient notificationRecipient,
                                                        List<SendableNotification> sendableNotifications) {
        final SummaryNotificationsDTO summaryNotificationsDTO = SummaryNotificationsDTO.from(notificationRecipient, sendableNotifications,
                customerIOProperties.getEnvironment());
        return new MailDTO<>(customerIOProperties.getWeeklyNotificationsEmailId().toString(),
                mapIdentifiers(notificationRecipient),
                notificationRecipient.email(),
                summaryNotificationsDTO.title(),
                summaryNotificationsDTO);
    }

    public static MailDTO<ProjectApplicationRefusedDTO> from(CustomerIOProperties customerIOProperties, SendableNotification notification,
                                                             ApplicationRefused applicationRefused) {
        final ProjectApplicationRefusedDTO projectApplicationRefusedDTO = ProjectApplicationRefusedDTO.fromEvent(notification.recipient().login(),
                applicationRefused, customerIOProperties.getEnvironment());
        return new MailDTO<>(customerIOProperties.getProjectApplicationRefusedEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                notification.recipient().email(),
                projectApplicationRefusedDTO.title(),
                projectApplicationRefusedDTO
        );
    }

    public record IdentifiersDTO(String id, String email) {
    }

    public static MailDTO<InvoiceRejectedDTO> from(
            @NonNull CustomerIOProperties customerIOProperties,
            @NonNull SendableNotification notification,
            @NonNull InvoiceRejected invoiceRejected) {
        return new MailDTO<>(customerIOProperties.getInvoiceRejectedEmailId().toString(),
                new IdentifiersDTO(notification.recipientId().toString(), null),
                notification.recipient().email(),
                "An invoice for %d reward(s) got rejected".formatted(invoiceRejected.rewards().size()),
                InvoiceRejectedDTO.fromEvent(notification.recipient().login(), invoiceRejected, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<VerificationClosedDTO> from(
            @NonNull CustomerIOProperties customerIOProperties,
            @NonNull SendableNotification notification,
            @NonNull BillingProfileVerificationClosed billingProfileVerificationClosed
    ) {
        final VerificationClosedDTO verificationClosedDTO = VerificationClosedDTO.fromEvent(notification.recipient().login(),
                billingProfileVerificationClosed, customerIOProperties.getEnvironment());
        return new MailDTO<>(customerIOProperties.getVerificationClosedEmailId().toString(),
                new IdentifiersDTO(notification.recipientId().toString(), null),
                notification.recipient().email(),
                verificationClosedDTO.title(),
                verificationClosedDTO);
    }

    public static MailDTO<RewardCreatedDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                 @NonNull SendableNotification notification,
                                                 @NonNull RewardReceived rewardReceived) {
        return new MailDTO<>(customerIOProperties.getNewRewardReceivedEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                notification.recipient().email(),
                "New reward received ✨",
                RewardCreatedDTO.fromEvent(notification.recipient().login(), rewardReceived, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<RewardCanceledDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                  @NonNull SendableNotification notification,
                                                  @NonNull RewardCanceled rewardCanceled) {
        return new MailDTO<>(
                customerIOProperties.getRewardCanceledEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                notification.recipient().email(),
                "Reward %s got canceled".formatted(rewardCanceled.shortReward().id().pretty()),
                RewardCanceledDTO.fromEvent(notification.recipient().login(), rewardCanceled, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<RewardsPaidDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                               @NonNull SendableNotification notification,
                                               @NonNull RewardsPaid rewardsPaid) {
        return new MailDTO<>(customerIOProperties.getRewardsPaidEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                notification.recipient().email(),
                "Your rewards are processed! 🥳",
                RewardsPaidDTO.fromEvent(notification.recipient().login(), rewardsPaid, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<NewCommitteeApplicationDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                           @NonNull SendableNotification notification,
                                                           @NonNull CommitteeApplicationCreated committeeApplicationCreated) {
        return new MailDTO<>(customerIOProperties.getNewCommitteeApplicationEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                notification.recipient().email(),
                "Your application to committee %s".formatted(committeeApplicationCreated.getCommitteeName()),
                NewCommitteeApplicationDTO.fromEvent(notification.recipient().login(), committeeApplicationCreated, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<ProjectApplicationAcceptedDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                              @NonNull SendableNotification notification,
                                                              @NonNull ApplicationAccepted applicationAccepted) {
        return new MailDTO<>(customerIOProperties.getProjectApplicationAcceptedEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                notification.recipient().email(),
                "Your application has been accepted!",
                ProjectApplicationAcceptedDTO.fromEvent(notification.recipient().login(), applicationAccepted, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<KycIdentityVerificationDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                           @NonNull BillingProfileChildrenKycVerification billingProfileChildrenKycVerification) {
        return new MailDTO<>(customerIOProperties.getKycIndividualVerificationEmailId().toString(),
                mapIdentifiers(billingProfileChildrenKycVerification.individualKycIdentity().email(), null),
                billingProfileChildrenKycVerification.individualKycIdentity().email(),
                "Verify your identity to validate your company",
                KycIdentityVerificationDTO.from(billingProfileChildrenKycVerification)
        );
    }

    public static MailDTO<CompleteYourBillingProfileDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                              @NonNull SendableNotification notification,
                                                              @NonNull CompleteYourBillingProfile completeYourBillingProfile) {
        return new MailDTO<>(customerIOProperties.getCompleteYourBillingProfileEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                notification.recipient().email(),
                "Complete your billing profile",
                CompleteYourBillingProfileDTO.from(completeYourBillingProfile, notification.recipient().login(), customerIOProperties.getEnvironment())
        );
    }


    public static MailDTO<VerificationRejectedDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                        @NonNull SendableNotification notification,
                                                        @NonNull BillingProfileVerificationRejected billingProfileVerificationRejected) {
        final VerificationRejectedDTO verificationRejectedDTO = VerificationRejectedDTO.fromEvent(notification.recipient().login(),
                billingProfileVerificationRejected, customerIOProperties.getEnvironment());
        return new MailDTO<>(customerIOProperties.getVerificationRejectedEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                notification.recipient().email(),
                verificationRejectedDTO.title(),
                verificationRejectedDTO);
    }

    public static MailDTO<IssueCreatedDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                @NonNull SendableNotification notification,
                                                @NonNull GoodFirstIssueCreated goodFirstIssueCreated) {
        final IssueCreatedDTO issueCreatedDTO = IssueCreatedDTO.from(notification.recipient().login(), goodFirstIssueCreated,
                customerIOProperties.getEnvironment());
        return new MailDTO<>(customerIOProperties.getIssueCreatedEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                notification.recipient().email(),
                issueCreatedDTO.title(),
                issueCreatedDTO);
    }

    private static IdentifiersDTO mapIdentifiers(@NonNull String email, UserId id) {
        return new IdentifiersDTO(isNull(id) ? null : id.toString(), isNull(id) ? email : null);
    }

    private static IdentifiersDTO mapIdentifiers(@NonNull NotificationRecipient user) {
        return new IdentifiersDTO(user.id().toString(), user.email());
    }
}
