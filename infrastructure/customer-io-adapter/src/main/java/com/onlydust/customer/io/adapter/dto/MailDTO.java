package com.onlydust.customer.io.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.UUID;

import static java.util.Objects.isNull;

@Builder
public record MailDTO<MessageData>(@NonNull @JsonProperty("transactional_message_id") String transactionalMessageId,
                                   @NonNull IdentifiersDTO identifiers,
                                   @NonNull String from,
                                   @NonNull String to,
                                   @NonNull String subject,
                                   @NonNull @JsonProperty("message_data") MessageData messageData
) {

    public MailDTO(@NonNull String transactionalMessageId, @NonNull IdentifiersDTO identifiers, @NonNull String from, @NonNull String to,
                   @NonNull String subject, @NonNull MessageData messageData) {
        this.transactionalMessageId = transactionalMessageId;
        this.identifiers = identifiers;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.messageData = messageData;
    }

    public record IdentifiersDTO(String id, String email) {
    }

    public static MailDTO<InvoiceRejectedDTO> from(
            @NonNull CustomerIOProperties customerIOProperties,
            @NonNull SendableNotification notification,
            @NonNull InvoiceRejected invoiceRejected) {
        return new MailDTO<>(customerIOProperties.getInvoiceRejectedEmailId().toString(),
                new IdentifiersDTO(notification.recipientId().toString(), null),
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "An invoice for %d reward(s) got rejected".formatted(invoiceRejected.rewards().size()),
                InvoiceRejectedDTO.fromEvent(notification.recipient().login(), invoiceRejected, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<VerificationFailedDTO> from(
            @NonNull CustomerIOProperties customerIOProperties,
            @NonNull SendableNotification notification,
            @NonNull BillingProfileVerificationFailed billingProfileVerificationFailed
    ) {
        return new MailDTO<>(customerIOProperties.getVerificationFailedEmailId().toString(),
                new IdentifiersDTO(notification.recipientId().toString(), null),
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "Your verification failed with status %s".formatted(billingProfileVerificationFailed.verificationStatus().name()),
                VerificationFailedDTO.fromEvent(notification.recipient().login(), billingProfileVerificationFailed));
    }

    public static MailDTO<RewardCreatedDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                 @NonNull SendableNotification notification,
                                                 @NonNull RewardReceived rewardReceived) {
        return new MailDTO<>(customerIOProperties.getNewRewardReceivedEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                customerIOProperties.getOnlyDustAdminEmail(),
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
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "Reward %s got canceled".formatted(rewardCanceled.shortReward().getId().pretty()),
                RewardCanceledDTO.fromEvent(notification.recipient().login(), rewardCanceled));
    }

    public static MailDTO<RewardsPaidDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                               @NonNull SendableNotification notification,
                                               @NonNull RewardsPaid rewardsPaid) {
        return new MailDTO<>(customerIOProperties.getRewardsPaidEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "Your rewards are processed! 🥳",
                RewardsPaidDTO.fromEvent(notification.recipient().login(), rewardsPaid, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<NewCommitteeApplicationDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                           @NonNull SendableNotification notification,
                                                           @NonNull CommitteeApplicationCreated committeeApplicationCreated) {
        return new MailDTO<>(customerIOProperties.getNewCommitteeApplicationEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                customerIOProperties.getOnlyDustMarketingEmail(),
                notification.recipient().email(),
                "Your application to committee %s".formatted(committeeApplicationCreated.getCommitteeName()),
                NewCommitteeApplicationDTO.fromEvent(notification.recipient().login(), committeeApplicationCreated, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<ProjectApplicationAcceptedDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                              @NonNull SendableNotification notification,
                                                              @NonNull ApplicationAccepted applicationAccepted) {
        return new MailDTO<>(customerIOProperties.getProjectApplicationAcceptedEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                customerIOProperties.getOnlyDustMarketingEmail(),
                notification.recipient().email(),
                "Your application has been accepted!",
                ProjectApplicationAcceptedDTO.fromEvent(notification.recipient().login(), applicationAccepted, customerIOProperties.getEnvironment()));
    }

    public static MailDTO<KycIdentityVerificationDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                           @NonNull BillingProfileChildrenKycVerification billingProfileChildrenKycVerification) {
        return new MailDTO<>(customerIOProperties.getKycIndividualVerificationEmailId().toString(),
                mapIdentifiers(billingProfileChildrenKycVerification.individualKycIdentity().email(), null),
                customerIOProperties.getOnlyDustAdminEmail(),
                billingProfileChildrenKycVerification.individualKycIdentity().email(),
                "Verified your identity to validate your company",
                KycIdentityVerificationDTO.from(billingProfileChildrenKycVerification)
        );
    }

    public static MailDTO<CompleteYourBillingProfileDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                              @NonNull SendableNotification notification,
                                                              @NonNull CompleteYourBillingProfile completeYourBillingProfile) {
        return new MailDTO<>(customerIOProperties.getCompleteYourBillingProfileEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "Complete your billing profile",
                CompleteYourBillingProfileDTO.from(completeYourBillingProfile, notification.recipient().login(), customerIOProperties.getEnvironment())
        );
    }

    private static IdentifiersDTO mapIdentifiers(@NonNull String email, UUID id) {
        return new IdentifiersDTO(isNull(id) ? null : id.toString(), isNull(id) ? email : null);
    }

    private static IdentifiersDTO mapIdentifiers(@NonNull NotificationRecipient user) {
        return new IdentifiersDTO(user.id().toString(), user.email());
    }
}
