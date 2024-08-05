package com.onlydust.customer.io.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationFailed;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.notification.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.accounting.domain.notification.RewardsPaid;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.math.RoundingMode;
import java.util.List;
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
                InvoiceRejectedDTO.fromEvent(notification.recipient().login(), invoiceRejected));
    }

    public static MailDTO<VerificationFailedDTO> fromVerificationFailed(
            @NonNull CustomerIOProperties customerIOProperties,
            @NonNull BillingProfileVerificationFailed billingProfileVerificationFailed
    ) {
        return new MailDTO<>(customerIOProperties.getVerificationFailedEmailId().toString(),
                new IdentifiersDTO(billingProfileVerificationFailed.ownerId().value().toString(), null),
                customerIOProperties.getOnlyDustAdminEmail(), billingProfileVerificationFailed.ownerEmail(),
                "Your verification failed with status %s".formatted(billingProfileVerificationFailed.verificationStatus().name()),
                VerificationFailedDTO.fromEvent(billingProfileVerificationFailed));
    }

    public static MailDTO<RewardCreatedDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                 @NonNull SendableNotification notification,
                                                 @NonNull RewardReceived rewardReceived) {
        return new MailDTO<>(customerIOProperties.getNewRewardReceivedEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "New reward received ✨",
                RewardCreatedDTO.fromEvent(notification.recipient().login(), rewardReceived));
    }

    public static MailDTO<RewardCanceledDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                  @NonNull SendableNotification notification,
                                                  @NonNull RewardCanceled rewardCanceled) {
        final var dto = RewardCanceledDTO.fromEvent(notification.recipient().login(), rewardCanceled);
        return new MailDTO<>(
                customerIOProperties.getRewardCanceledEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "Reward %s got canceled".formatted(dto.rewardName()),
                dto);
    }

    public static MailDTO<RewardsPaidDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                               @NonNull SendableNotification notification,
                                               @NonNull RewardsPaid rewardsPaid) {
        return new MailDTO<>(customerIOProperties.getRewardsPaidEmailId().toString(),
                mapIdentifiers(notification.recipient().email(), notification.recipientId()),
                customerIOProperties.getOnlyDustAdminEmail(),
                notification.recipient().email(),
                "Your rewards are processed! 🥳",
                RewardsPaidDTO.fromEvent(notification.recipient().login(), rewardsPaid));
    }

    public static MailDTO<NewCommitteeApplicationDTO> from(@NonNull CustomerIOProperties customerIOProperties,
                                                           @NonNull SendableNotification notification,
                                                           @NonNull CommitteeApplicationCreated committeeApplicationCreated) {
        return new MailDTO<>(customerIOProperties.getNewCommitteeApplicationEmailId().toString(),
                mapIdentifiers(notification.recipient()),
                customerIOProperties.getOnlyDustMarketingEmail(),
                notification.recipient().email(),
                "Your application to committee %s".formatted(committeeApplicationCreated.getCommitteeName()),
                NewCommitteeApplicationDTO.fromEvent(notification.recipient().login(), committeeApplicationCreated));
    }

    public static MailDTO<ProjectApplicationsToReviewByUserDTO> fromProjectApplicationsToReviewByUser(@NonNull CustomerIOProperties customerIOProperties,
                                                                                                      @NonNull ProjectApplicationsToReviewByUser projectApplicationsToReviewByUser) {
        return new MailDTO<>(customerIOProperties.getProjectApplicationsToReviewByUserEmailId().toString(),
                mapIdentifiers(projectApplicationsToReviewByUser.getEmail(), projectApplicationsToReviewByUser.getUserId()),
                customerIOProperties.getOnlyDustMarketingEmail(),
                projectApplicationsToReviewByUser.getEmail(),
                "Applications to review daily report",
                ProjectApplicationsToReviewByUserDTO.fromEvent(projectApplicationsToReviewByUser));
    }

    public static MailDTO<ProjectApplicationAcceptedDTO> fromProjectApplicationAccepted(@NonNull CustomerIOProperties customerIOProperties,
                                                                                        @NonNull ProjectApplicationAccepted projectApplicationAccepted) {
        return new MailDTO<>(customerIOProperties.getProjectApplicationAcceptedEmailId().toString(),
                mapIdentifiers(projectApplicationAccepted.getEmail(), projectApplicationAccepted.getUserId()),
                customerIOProperties.getOnlyDustMarketingEmail(),
                projectApplicationAccepted.getEmail(),
                "Your application has been accepted!",
                ProjectApplicationAcceptedDTO.fromEvent(projectApplicationAccepted));
    }

    private static IdentifiersDTO mapIdentifiers(@NonNull String email, UUID id) {
        return new IdentifiersDTO(isNull(id) ? null : id.toString(), isNull(id) ? email : null);
    }

    private static IdentifiersDTO mapIdentifiers(@NonNull NotificationRecipient user) {
        return new IdentifiersDTO(user.id().toString(), user.email());
    }

    public static String getRewardNames(List<ShortReward> rewards) {
        return String.join("<br>", rewards.stream()
                .map(r -> String.join(" - ", r.getId().pretty(), r.getProjectName(), r.getCurrencyCode(),
                        r.getAmount().setScale(3, RoundingMode.HALF_UP).toString()))
                .toList());
    }
}
