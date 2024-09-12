package onlydust.com.marketplace.api.read.mapper;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.user.NotificationReadEntity;
import onlydust.com.marketplace.api.read.repositories.CurrencyReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProjectLinkReadRepository;
import onlydust.com.marketplace.api.read.repositories.SponsorReadRepository;
import onlydust.com.marketplace.project.domain.model.notification.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class NotificationMapper {
    final ProjectLinkReadRepository projectLinkReadRepository;
    final SponsorReadRepository sponsorReadRepository;
    final ProgramReadRepository programReadRepository;
    final CurrencyReadRepository currencyReadRepository;

    public NotificationPageItemResponse toNotificationPageItemResponse(final NotificationReadEntity entity) {
        final var notification = entity.getData().notification();
        final var responseData = new NotificationPageItemResponseData();
        NotificationType notificationType;

        if (notification instanceof CommitteeApplicationCreated committeeApplicationCreated) {
            notificationType = map(committeeApplicationCreated, responseData);
        } else if (notification instanceof RewardReceived rewardReceived) {
            notificationType = map(rewardReceived, responseData);
        } else if (notification instanceof RewardCanceled rewardCanceled) {
            notificationType = map(rewardCanceled, responseData);
        } else if (notification instanceof InvoiceRejected invoiceRejected) {
            notificationType = map(invoiceRejected, responseData);
        } else if (notification instanceof RewardsPaid rewardsPaid) {
            notificationType = map(rewardsPaid, responseData);
        } else if (notification instanceof ApplicationToReview applicationToReview) {
            notificationType = map(applicationToReview, responseData);
        } else if (notification instanceof ApplicationAccepted applicationAccepted) {
            notificationType = map(applicationAccepted, responseData);
        } else if (notification instanceof ApplicationRefused applicationRefused) {
            notificationType = map(applicationRefused, responseData);
        } else if (notification instanceof GoodFirstIssueCreated goodFirstIssueCreated) {
            notificationType = map(goodFirstIssueCreated, responseData);
        } else if (notification instanceof BillingProfileVerificationClosed billingProfileVerificationClosed) {
            notificationType = map(billingProfileVerificationClosed, responseData);
        } else if (notification instanceof BillingProfileVerificationRejected billingProfileVerificationRejected) {
            notificationType = map(billingProfileVerificationRejected, responseData);
        } else if (notification instanceof CompleteYourBillingProfile completeYourBillingProfile) {
            notificationType = map(completeYourBillingProfile, responseData);
        } else if (notification instanceof FundsAllocatedToProgram fundsAllocatedToProgram) {
            notificationType = map(fundsAllocatedToProgram, responseData);
        } else if (notification instanceof FundsUnallocatedFromProgram fundsUnallocatedFromProgram) {
            notificationType = map(fundsUnallocatedFromProgram, responseData);
        } else if (notification instanceof DepositRejected depositRejected) {
            notificationType = map(depositRejected, responseData);
        } else if (notification instanceof DepositApproved depositApproved) {
            notificationType = map(depositApproved, responseData);
        } else {
            throw internalServerError("Unknown notification data type %s".formatted(notification.getClass().getSimpleName()));
        }

        return new NotificationPageItemResponse()
                .id(entity.getId())
                .type(notificationType)
                .status(Boolean.TRUE.equals(entity.isRead()) ? NotificationStatus.READ : NotificationStatus.UNREAD)
                .timestamp(entity.getCreatedAt())
                .data(responseData);
    }

    private @NotNull NotificationType map(DepositRejected depositRejected,
                                          NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.SPONSOR_LEAD_DEPOSIT_REJECTED;
        final var sponsor = sponsorReadRepository.findById(depositRejected.sponsorId().value())
                .orElseThrow(() -> internalServerError("Sponsor %s doesn't exist".formatted(depositRejected.sponsorId())));
        final var currency = currencyReadRepository.findById(depositRejected.currencyId())
                .orElseThrow(() -> internalServerError("Currency %s doesn't exist".formatted(depositRejected.currencyId())));

        notificationPageItemResponseData.setSponsorLeadDepositRejected(new NotificationSponsorLeadDepositRejected(
                sponsor.toLinkResponse(),
                depositRejected.amount(),
                currency.code(),
                depositRejected.timestamp()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(DepositApproved depositApproved,
                                          NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.SPONSOR_LEAD_DEPOSIT_APPROVED;
        final var sponsor = sponsorReadRepository.findById(depositApproved.sponsorId().value())
                .orElseThrow(() -> internalServerError("Sponsor %s doesn't exist".formatted(depositApproved.sponsorId())));
        final var currency = currencyReadRepository.findById(depositApproved.currencyId())
                .orElseThrow(() -> internalServerError("Currency %s doesn't exist".formatted(depositApproved.currencyId())));

        notificationPageItemResponseData.setSponsorLeadDepositApproved(new NotificationSponsorLeadDepositApproved(
                sponsor.toLinkResponse(),
                depositApproved.amount(),
                currency.code(),
                depositApproved.timestamp()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(FundsUnallocatedFromProgram fundsUnallocatedFromProgram,
                                          NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.SPONSOR_LEAD_FUNDS_UNALLOCATED_FROM_PROGRAM;
        final var sponsor = sponsorReadRepository.findById(fundsUnallocatedFromProgram.sponsorId().value())
                .orElseThrow(() -> internalServerError("Sponsor %s doesn't exist".formatted(fundsUnallocatedFromProgram.sponsorId())));
        final var program = programReadRepository.findById(fundsUnallocatedFromProgram.programId().value())
                .orElseThrow(() -> internalServerError("Program %s doesn't exist".formatted(fundsUnallocatedFromProgram.programId())));
        final var currency = currencyReadRepository.findById(fundsUnallocatedFromProgram.currencyId())
                .orElseThrow(() -> internalServerError("Currency %s doesn't exist".formatted(fundsUnallocatedFromProgram.currencyId())));

        notificationPageItemResponseData.setSponsorLeadFundsUnallocatedFromProgram(new NotificationSponsorLeadFundsUnallocatedFromProgram(
                program.toLinkResponse(),
                sponsor.toLinkResponse(),
                fundsUnallocatedFromProgram.amount(),
                currency.code()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(FundsAllocatedToProgram fundsAllocatedToProgram, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.PROGRAM_LEAD_FUNDS_ALLOCATED_TO_PROGRAM;
        final var sponsor = sponsorReadRepository.findById(fundsAllocatedToProgram.sponsorId().value())
                .orElseThrow(() -> internalServerError("Sponsor %s doesn't exist".formatted(fundsAllocatedToProgram.sponsorId())));
        final var program = programReadRepository.findById(fundsAllocatedToProgram.programId().value())
                .orElseThrow(() -> internalServerError("Program %s doesn't exist".formatted(fundsAllocatedToProgram.programId())));
        final var currency = currencyReadRepository.findById(fundsAllocatedToProgram.currencyId())
                .orElseThrow(() -> internalServerError("Currency %s doesn't exist".formatted(fundsAllocatedToProgram.currencyId())));

        notificationPageItemResponseData.setProgramLeadFundsAllocatedToProgram(new NotificationProgramLeadFundsAllocatedToProgram(
                program.toLinkResponse(),
                sponsor.toLinkResponse(),
                fundsAllocatedToProgram.amount(),
                currency.code()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(CompleteYourBillingProfile completeYourBillingProfile,
                                          NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.GLOBAL_BILLING_PROFILE_REMINDER;
        notificationPageItemResponseData.setGlobalBillingProfileReminder(new NotificationGlobalBillingProfileReminder(
                completeYourBillingProfile.billingProfile().billingProfileId().value(),
                completeYourBillingProfile.billingProfile().billingProfileName()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(BillingProfileVerificationRejected billingProfileVerificationRejected,
                                          NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.GLOBAL_BILLING_PROFILE_VERIFICATION_REJECTED;
        notificationPageItemResponseData.setGlobalBillingProfileVerificationRejected(new NotificationGlobalBillingProfileVerificationRejected(
                billingProfileVerificationRejected.billingProfileId().value(),
                billingProfileVerificationRejected.billingProfileName(),
                billingProfileVerificationRejected.rejectionReason())
        );
        return notificationType;
    }

    private @NotNull NotificationType map(BillingProfileVerificationClosed billingProfileVerificationClosed,
                                          NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.GLOBAL_BILLING_PROFILE_VERIFICATION_CLOSED;
        notificationPageItemResponseData.setGlobalBillingProfileVerificationClosed(new NotificationGlobalBillingProfileVerificationClosed(
                billingProfileVerificationClosed.billingProfileId().value(),
                billingProfileVerificationClosed.billingProfileName())
        );
        return notificationType;
    }

    private @NotNull NotificationType map(GoodFirstIssueCreated goodFirstIssueCreated, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.CONTRIBUTOR_PROJECT_GOOD_FIRST_ISSUE_CREATED;
        final ProjectLinkReadEntity projectLinkReadEntity = projectLinkReadRepository.findById(goodFirstIssueCreated.getProject().id().value())
                .orElseThrow(() -> internalServerError(("Project %s must exist").formatted(goodFirstIssueCreated.getProject())));
        notificationPageItemResponseData.setContributorProjectGoodFirstIssueCreated(new NotificationContributorProjectGoodFirstIssueCreated(
                projectLinkReadEntity.name(),
                projectLinkReadEntity.slug(),
                goodFirstIssueCreated.getIssue().id(),
                goodFirstIssueCreated.getIssue().title()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(ApplicationRefused applicationRefused, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.CONTRIBUTOR_PROJECT_APPLICATION_REFUSED;
        final ProjectLinkReadEntity projectLinkReadEntity = projectLinkReadRepository.findById(applicationRefused.getProject().id().value())
                .orElseThrow(() -> internalServerError(("Project %s must exist").formatted(applicationRefused.getProject())));
        notificationPageItemResponseData.setContributorProjectApplicationRefused(new NotificationContributorProjectApplicationRefused(
                projectLinkReadEntity.name(),
                projectLinkReadEntity.slug(),
                applicationRefused.getIssue().id(),
                applicationRefused.getIssue().title()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(ApplicationAccepted applicationAccepted, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.CONTRIBUTOR_PROJECT_APPLICATION_ACCEPTED;
        final ProjectLinkReadEntity projectLinkReadEntity = projectLinkReadRepository.findById(applicationAccepted.getProject().id().value())
                .orElseThrow(() -> internalServerError(("Project %s must exist").formatted(applicationAccepted.getProject())));
        notificationPageItemResponseData.setContributorProjectApplicationAccepted(new NotificationContributorProjectApplicationAccepted(
                projectLinkReadEntity.name(),
                projectLinkReadEntity.slug(),
                applicationAccepted.getIssue().id(),
                applicationAccepted.getIssue().title()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(ApplicationToReview applicationToReview, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.MAINTAINER_APPLICATION_TO_REVIEW;
        final ProjectLinkReadEntity projectLinkReadEntity = projectLinkReadRepository.findById(applicationToReview.getProject().id().value())
                .orElseThrow(() -> internalServerError(("Project %s must exist").formatted(applicationToReview.getProject())));
        notificationPageItemResponseData.setMaintainerApplicationToReview(new NotificationMaintainerApplicationToReview(
                projectLinkReadEntity.slug(),
                projectLinkReadEntity.name(),
                applicationToReview.getUser().githubId(),
                applicationToReview.getIssue().id(),
                applicationToReview.getIssue().title(),
                applicationToReview.getUser().login()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(RewardsPaid rewardsPaid, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.CONTRIBUTOR_REWARDS_PAID;
        notificationPageItemResponseData.setContributorRewardsPaid(new NotificationContributorRewardsPaid(
                rewardsPaid.shortRewards().size(),
                rewardsPaid.shortRewards().stream().map(ShortReward::getDollarsEquivalent).reduce(BigDecimal.ZERO, BigDecimal::add)
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(InvoiceRejected invoiceRejected, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.CONTRIBUTOR_INVOICE_REJECTED;
        notificationPageItemResponseData.setContributorInvoiceRejected(new NotificationContributorInvoiceRejected(
                invoiceRejected.invoiceName(),
                invoiceRejected.rejectionReason(),
                invoiceRejected.billingProfileId()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(RewardCanceled rewardCanceled, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.CONTRIBUTOR_REWARD_CANCELED;
        notificationPageItemResponseData.setContributorRewardCanceled(new NotificationContributorRewardCanceled(
                rewardCanceled.shortReward().getId().value(),
                rewardCanceled.shortReward().getProjectName(),
                rewardCanceled.shortReward().getAmount(),
                rewardCanceled.shortReward().getCurrencyCode()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(RewardReceived rewardReceived, NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationType = NotificationType.CONTRIBUTOR_REWARD_RECEIVED;
        notificationPageItemResponseData.setContributorRewardReceived(new NotificationContributorRewardReceived(
                rewardReceived.shortReward().getId().value(),
                rewardReceived.shortReward().getProjectName(),
                rewardReceived.shortReward().getAmount(),
                rewardReceived.shortReward().getCurrencyCode(),
                rewardReceived.sentByGithubLogin(),
                rewardReceived.contributionCount()
        ));
        return notificationType;
    }

    private @NotNull NotificationType map(CommitteeApplicationCreated committeeApplicationCreated,
                                          NotificationPageItemResponseData notificationPageItemResponseData) {
        NotificationType notificationType;
        notificationPageItemResponseData.setMaintainerCommitteeApplicationCreated(new NotificationMaintainerCommitteeApplicationCreated()
                .committeeName(committeeApplicationCreated.getCommitteeName())
                .committeeId(committeeApplicationCreated.getCommitteeId().value())
        );
        notificationType = NotificationType.MAINTAINER_COMMITTEE_APPLICATION_CREATED;
        return notificationType;
    }
}
