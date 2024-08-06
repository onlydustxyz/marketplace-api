package onlydust.com.marketplace.api.read.entities.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.api.read.repositories.PublicProjectReadRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationTypeIdResolver;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationToReview;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "notifications", schema = "iam")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
public class NotificationReadEntity {

    @Id
    UUID id;
    @Column(nullable = false)
    UUID recipientId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NonNull
    NotificationCategory category;
    @Column(nullable = false)
    ZonedDateTime createdAt;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @NonNull
    NotificationEntity.Data data;
    boolean isRead;


    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(NotificationTypeIdResolver.class)
        private NotificationData notification;
    }


    public static Boolean isReadFromContract(final NotificationStatus status) {
        return isNull(status) ? null : switch (status) {
            case READ -> true;
            case UNREAD -> false;
        };
    }

    public NotificationPageItemResponse toNotificationPageItemResponse(final PublicProjectReadRepository publicProjectReadRepository) {
        final NotificationPageItemResponseData notificationPageItemResponseData = new NotificationPageItemResponseData();
        NotificationType notificationType = null;
        if (data.notification() instanceof CommitteeApplicationCreated committeeApplicationCreated) {
            notificationPageItemResponseData.setMaintainerCommitteeApplicationCreated(new NotificationMaintainerCommitteeApplicationCreated()
                    .committeeName(committeeApplicationCreated.getCommitteeName())
                    .committeeId(committeeApplicationCreated.getCommitteeId())
            );
            notificationType = NotificationType.MAINTAINER_COMMITTEE_APPLICATION_CREATED;
        } else if (data.notification() instanceof RewardReceived rewardReceived) {
            notificationType = NotificationType.CONTRIBUTOR_REWARD_RECEIVED;
            notificationPageItemResponseData.setContributorRewardReceived(new NotificationContributorRewardReceived(
                    rewardReceived.shortReward().getId().value(),
                    rewardReceived.shortReward().getProjectName(),
                    rewardReceived.shortReward().getAmount(),
                    rewardReceived.shortReward().getCurrencyCode(),
                    rewardReceived.sentByGithubLogin(),
                    rewardReceived.contributionCount()
            ));
        } else if (data.notification() instanceof RewardCanceled rewardCanceled) {
            notificationType = NotificationType.CONTRIBUTOR_REWARD_CANCELED;
            notificationPageItemResponseData.setContributorRewardCanceled(new NotificationContributorRewardCanceled(
                    rewardCanceled.shortReward().getId().value(),
                    rewardCanceled.shortReward().getProjectName(),
                    rewardCanceled.shortReward().getAmount(),
                    rewardCanceled.shortReward().getCurrencyCode()
            ));
        } else if (data.notification() instanceof InvoiceRejected invoiceRejected) {
            notificationType = NotificationType.CONTRIBUTOR_INVOICE_REJECTED;
            notificationPageItemResponseData.setContributorInvoiceRejected(new NotificationContributorInvoiceRejected(
                    invoiceRejected.invoiceName(),
                    invoiceRejected.rejectionReason(),
                    invoiceRejected.billingProfileId()
            ));
        } else if (data.notification() instanceof RewardsPaid rewardsPaid) {
            notificationType = NotificationType.CONTRIBUTOR_REWARDS_PAID;
            notificationPageItemResponseData.setContributorRewardsPaid(new NotificationContributorRewardsPaid(
                    rewardsPaid.shortRewards().size(),
                    rewardsPaid.shortRewards().stream().map(ShortReward::getDollarsEquivalent).reduce(BigDecimal.ZERO, BigDecimal::add)
            ));
        } else if (data.notification() instanceof ApplicationToReview applicationToReview) {
            notificationType = NotificationType.MAINTAINER_APPLICATION_TO_REVIEW;
            final PublicProjectReadEntity projectReadEntity = publicProjectReadRepository.findById(applicationToReview.getProject().id())
                    .orElseThrow(() -> OnlyDustException.internalServerError(("Project %s must exist").formatted(applicationToReview.getProject())));
            notificationPageItemResponseData.setMaintainerApplicationToReview(new NotificationMaintainerApplicationToReview(
                    projectReadEntity.getSlug(),
                    projectReadEntity.getName(),
                    applicationToReview.getUser().githubId(),
                    applicationToReview.getIssue().id(),
                    applicationToReview.getIssue().title(),
                    applicationToReview.getUser().login()
            ));
        } else if (data.notification() instanceof ApplicationAccepted applicationAccepted) {
            notificationType = NotificationType.CONTRIBUTOR_PROJECT_APPLICATION_ACCEPTED;
            final PublicProjectReadEntity projectReadEntity = publicProjectReadRepository.findById(applicationAccepted.getProject().id())
                    .orElseThrow(() -> OnlyDustException.internalServerError(("Project %s must exist").formatted(applicationAccepted.getProject())));
            notificationPageItemResponseData.setContributorProjectApplicationAccepted(new NotificationContributorProjectApplicationAccepted(
                    projectReadEntity.getName(),
                    projectReadEntity.getSlug(),
                    applicationAccepted.getIssue().id(),
                    applicationAccepted.getIssue().title()
            ));
        } else if (data.notification() instanceof BillingProfileVerificationFailed billingProfileVerificationFailed) {
            notificationType = NotificationType.GLOBAL_BILLING_PROFILE_VERIFICATION_FAILED;
            notificationPageItemResponseData.setGlobalBillingProfileVerificationFailed(new NotificationGlobalBillingProfileVerificationFailed(
                    billingProfileVerificationFailed.billingProfileId().value(),
                    null,
                    switch (billingProfileVerificationFailed.verificationStatus()) {
                        case CLOSED -> VerificationStatus.CLOSED;
                        case VERIFIED -> VerificationStatus.VERIFIED;
                        case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
                        case REJECTED -> VerificationStatus.REJECTED;
                        case STARTED -> VerificationStatus.STARTED;
                        case NOT_STARTED -> VerificationStatus.NOT_STARTED;
                    }
            ));
        } else {
            throw OnlyDustException.internalServerError("Unknown notification data type %s".formatted(data.notification().getClass().getSimpleName()));
        }

        return new NotificationPageItemResponse()
                .id(id)
                .type(notificationType)
                .status(Boolean.TRUE.equals(isRead) ? NotificationStatus.READ : NotificationStatus.UNREAD)
                .timestamp(createdAt)
                .data(notificationPageItemResponseData);
    }
}
