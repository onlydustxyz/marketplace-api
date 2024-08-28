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
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.api.read.repositories.ProjectLinkReadRepository;
import onlydust.com.marketplace.api.read.repositories.PublicProjectReadRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationTypeIdResolver;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationToReview;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.project.domain.model.notification.dto.ApplicationRefused;
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

    public NotificationPageItemResponse toNotificationPageItemResponse(final ProjectLinkReadRepository projectLinkReadRepository) {
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
            final ProjectLinkReadEntity projectLinkReadEntity = projectLinkReadRepository.findById(applicationToReview.getProject().id())
                    .orElseThrow(() -> OnlyDustException.internalServerError(("Project %s must exist").formatted(applicationToReview.getProject())));
            notificationPageItemResponseData.setMaintainerApplicationToReview(new NotificationMaintainerApplicationToReview(
                    projectLinkReadEntity.slug(),
                    projectLinkReadEntity.name(),
                    applicationToReview.getUser().githubId(),
                    applicationToReview.getIssue().id(),
                    applicationToReview.getIssue().title(),
                    applicationToReview.getUser().login()
            ));
        } else if (data.notification() instanceof ApplicationAccepted applicationAccepted) {
            notificationType = NotificationType.CONTRIBUTOR_PROJECT_APPLICATION_ACCEPTED;
            final ProjectLinkReadEntity projectLinkReadEntity = projectLinkReadRepository.findById(applicationAccepted.getProject().id())
                    .orElseThrow(() -> OnlyDustException.internalServerError(("Project %s must exist").formatted(applicationAccepted.getProject())));
            notificationPageItemResponseData.setContributorProjectApplicationAccepted(new NotificationContributorProjectApplicationAccepted(
                    projectLinkReadEntity.name(),
                    projectLinkReadEntity.slug(),
                    applicationAccepted.getIssue().id(),
                    applicationAccepted.getIssue().title()
            ));
        } else if (data.notification() instanceof ApplicationRefused applicationRefused) {
            notificationType = NotificationType.CONTRIBUTOR_PROJECT_APPLICATION_REFUSED;
            final ProjectLinkReadEntity projectLinkReadEntity = projectLinkReadRepository.findById(applicationRefused.getProject().id())
                    .orElseThrow(() -> OnlyDustException.internalServerError(("Project %s must exist").formatted(applicationRefused.getProject())));
            notificationPageItemResponseData.setContributorProjectApplicationRefused(new NotificationContributorProjectApplicationRefused(
                    projectLinkReadEntity.name(),
                    projectLinkReadEntity.slug(),
                    applicationRefused.getIssue().id(),
                    applicationRefused.getIssue().title()
            ));
        } else if (data.notification() instanceof BillingProfileVerificationClosed billingProfileVerificationClosed) {
            notificationType = NotificationType.GLOBAL_BILLING_PROFILE_VERIFICATION_CLOSED;
            notificationPageItemResponseData.setGlobalBillingProfileVerificationClosed(new NotificationGlobalBillingProfileVerificationClosed(
                    billingProfileVerificationClosed.billingProfileId().value(),
                    billingProfileVerificationClosed.billingProfileName())
            );
        } else if (data.notification() instanceof BillingProfileVerificationRejected billingProfileVerificationRejected) {
            notificationType = NotificationType.GLOBAL_BILLING_PROFILE_VERIFICATION_REJECTED;
            notificationPageItemResponseData.setGlobalBillingProfileVerificationRejected(new NotificationGlobalBillingProfileVerificationRejected(
                    billingProfileVerificationRejected.billingProfileId().value(),
                    billingProfileVerificationRejected.billingProfileName(),
                    billingProfileVerificationRejected.rejectionReason())
            );
        } else if (data.notification() instanceof CompleteYourBillingProfile completeYourBillingProfile) {
            notificationType = NotificationType.GLOBAL_BILLING_PROFILE_REMINDER;
            notificationPageItemResponseData.setGlobalBillingProfileReminder(new NotificationGlobalBillingProfileReminder(
                    completeYourBillingProfile.billingProfile().billingProfileId(),
                    completeYourBillingProfile.billingProfile().billingProfileName()
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
