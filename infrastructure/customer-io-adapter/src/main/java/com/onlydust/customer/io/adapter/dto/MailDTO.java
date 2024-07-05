package com.onlydust.customer.io.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.*;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.project.domain.model.event.NewCommitteeApplication;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;

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

    public static MailDTO<InvoiceRejectedDTO> fromInvoiceRejected(
            @NonNull CustomerIOProperties customerIOProperties,
            @NonNull InvoiceRejected invoiceRejected) {
        return new MailDTO<>(customerIOProperties.getInvoiceRejectedEmailId().toString(), new IdentifiersDTO(invoiceRejected.billingProfileAdminId().toString(),
                null),
                customerIOProperties.getOnlyDustAdminEmail(), invoiceRejected.billingProfileAdminEmail(),
                "An invoice for %s reward(s) got rejected".formatted(invoiceRejected.rewardCount()),
                InvoiceRejectedDTO.fromEvent(invoiceRejected));
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

    public static MailDTO<RewardCreatedDTO> fromRewardCreated(
            @NonNull CustomerIOProperties customerIOProperties,
            @NonNull RewardCreatedMailEvent rewardCreated
    ) {
        return new MailDTO<>(customerIOProperties.getNewRewardReceivedEmailId().toString(), mapIdentifiers(rewardCreated.email(),
                rewardCreated.recipientId())
                , customerIOProperties.getOnlyDustAdminEmail(), rewardCreated.email(),
                "New reward received ✨", RewardCreatedDTO.fromEvent(rewardCreated));
    }

    public static MailDTO<RewardCanceledDTO> fromRewardCanceled(@NonNull CustomerIOProperties customerIOProperties,
                                                                @NonNull RewardCanceled rewardCanceled) {
        final RewardCanceledDTO rewardCanceledDTO = RewardCanceledDTO.fromEvent(rewardCanceled);
        return new MailDTO<>(customerIOProperties.getRewardCanceledEmailId().toString(), mapIdentifiers(rewardCanceled.recipientEmail(),
                rewardCanceled.recipientId()),
                customerIOProperties.getOnlyDustAdminEmail(), rewardCanceled.recipientEmail(),
                "Reward %s got canceled".formatted(rewardCanceledDTO.rewardName()), rewardCanceledDTO);
    }

    public static MailDTO<RewardsPaidDTO> fromRewardsPaid(@NonNull CustomerIOProperties customerIOProperties,
                                                          @NonNull RewardsPaid rewardsPaid) {
        return new MailDTO<>(customerIOProperties.getRewardsPaidEmailId().toString(), mapIdentifiers(rewardsPaid.recipientEmail(), rewardsPaid.recipientId()),
                customerIOProperties.getOnlyDustAdminEmail(), rewardsPaid.recipientEmail(), "Your rewards are processed! 🥳",
                RewardsPaidDTO.fromEvent(rewardsPaid));
    }

    public static MailDTO<NewCommitteeApplicationDTO> fromNewCommitteeApplication(@NonNull CustomerIOProperties customerIOProperties,
                                                                                  @NonNull NewCommitteeApplication newCommitteeApplication) {
        return new MailDTO<>(customerIOProperties.getNewCommitteeApplicationEmailId().toString(), mapIdentifiers(newCommitteeApplication.getEmail(),
                newCommitteeApplication.getUserId()), customerIOProperties.getOnlyDustMarketingEmail(), newCommitteeApplication.getEmail(),
                "Your application to committee %s".formatted(newCommitteeApplication.getCommitteeName()),
                NewCommitteeApplicationDTO.fromEvent(newCommitteeApplication));
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

    private static IdentifiersDTO mapIdentifiers(@NonNull String email, UUID id) {
        return new IdentifiersDTO(isNull(id) ? null : id.toString(), isNull(id) ? email : null);
    }

    public static String getRewardNames(List<ShortReward> rewards) {
        return String.join("<br>", rewards.stream()
                .map(r -> String.join(" - ", r.getId().pretty(), r.getProjectName(), r.getCurrencyCode(),
                        r.getAmount().setScale(3, RoundingMode.HALF_UP).toString()))
                .toList());
    }
}
