package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.job.OutboxConsumer;
import onlydust.com.marketplace.project.domain.job.OutboxSkippingException;
import onlydust.com.marketplace.project.domain.model.OldBillingProfileType;
import onlydust.com.marketplace.project.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;
import onlydust.com.marketplace.project.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.project.domain.model.notification.Event;
import onlydust.com.marketplace.project.domain.port.input.AccountingUserObserverPort;
import onlydust.com.marketplace.project.domain.port.input.UserVerificationFacadePort;
import onlydust.com.marketplace.project.domain.port.output.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class UserVerificationService implements UserVerificationFacadePort, OutboxConsumer {

    private final OutboxPort outboxPort;
    private final Function<Event, BillingProfileUpdated> billingProfileExternalMapper;
    private final OldBillingProfileStoragePort oldBillingProfileStoragePort;
    private final UserVerificationStoragePort userVerificationStoragePort;
    private final AccountingUserObserverPort userObserver;
    private final NotificationPort notificationPort;
    private final UserStoragePort userStoragePort;
    private final WebhookPort webhookPort;

    @Override
    public void consumeUserVerificationEvent(Event event) {
        outboxPort.push(event);
    }

    @Override
    @Transactional
    public void process(Event event) {
        BillingProfileUpdated billingProfileUpdated = billingProfileExternalMapper.apply(event);
        if (billingProfileUpdated.isLinkedToAParentBillingProfile()) {
            billingProfileUpdated = processChildrenBillingProfile(billingProfileUpdated);
        } else {
            billingProfileUpdated = processParentBillingProfile(billingProfileUpdated);
        }
        billingProfileUpdated = updateBillingProfileEventWithUserData(billingProfileUpdated);
        userObserver.onBillingProfileUpdated(billingProfileUpdated);
        webhookPort.send(billingProfileUpdated);
        notificationPort.notifyNewVerificationEvent(billingProfileUpdated);
    }

    private BillingProfileUpdated processParentBillingProfile(BillingProfileUpdated billingProfileUpdated) {
        final UUID userId = switch (billingProfileUpdated.getType()) {
            case COMPANY -> updateCompanyProfile(billingProfileUpdated).getUserId();
            case INDIVIDUAL -> updateIndividualProfile(billingProfileUpdated).getUserId();
        };
        billingProfileUpdated = billingProfileUpdated.toBuilder().userId(userId).build();
        return billingProfileUpdated;
    }

    private BillingProfileUpdated processChildrenBillingProfile(final BillingProfileUpdated childrendBillingProfileUpdated) {
        final OldCompanyBillingProfile parentCompanyBillingProfile =
                oldBillingProfileStoragePort.findCompanyByExternalVerificationId(childrendBillingProfileUpdated.getParentExternalApplicantId())
                        .orElseThrow(() -> new OutboxSkippingException("Parent billing profile not found for external parent id %s"
                                .formatted(childrendBillingProfileUpdated.getParentExternalApplicantId())));
        oldBillingProfileStoragePort.saveChildrenKyc(childrendBillingProfileUpdated.getExternalApplicantId(),
                childrendBillingProfileUpdated.getParentExternalApplicantId(),
                childrendBillingProfileUpdated.getOldVerificationStatus());
        final OldCompanyBillingProfile updatedCompanyBillingProfile =
                parentCompanyBillingProfile.updateStatusFromNewChildrenStatuses(oldBillingProfileStoragePort.findKycStatusesFromParentKybExternalVerificationId(childrendBillingProfileUpdated.getParentExternalApplicantId()));
        final OldCompanyBillingProfile companyBillingProfile = oldBillingProfileStoragePort.saveCompanyProfile(updatedCompanyBillingProfile);
        return BillingProfileUpdated.builder()
                .billingProfileId(companyBillingProfile.getId())
                .type(OldBillingProfileType.COMPANY)
                .userId(companyBillingProfile.getUserId())
                .oldVerificationStatus(companyBillingProfile.getStatus())
                .rawReviewDetails(childrendBillingProfileUpdated.getRawReviewDetails())
                .build();
    }

    private OldCompanyBillingProfile updateCompanyProfile(final BillingProfileUpdated billingProfileUpdated) {
        return oldBillingProfileStoragePort.saveCompanyProfile(
                oldBillingProfileStoragePort.findCompanyProfileById(billingProfileUpdated.getBillingProfileId())
                        .map(companyBillingProfile -> companyBillingProfile.toBuilder()
                                .status(billingProfileUpdated.getOldVerificationStatus())
                                .reviewMessageForApplicant(billingProfileUpdated.getReviewMessageForApplicant())
                                .externalApplicantId(billingProfileUpdated.getExternalApplicantId())
                                .build())
                        .map(companyBillingProfile -> companyBillingProfile.updateStatusFromNewChildrenStatuses(
                                oldBillingProfileStoragePort.findKycStatusesFromParentKybExternalVerificationId(companyBillingProfile.getExternalApplicantId()))
                        )
                        .map(companyBillingProfile1 -> userVerificationStoragePort.updateCompanyVerification(companyBillingProfile1))
                        .orElseThrow(() -> new OutboxSkippingException(String.format("Skipping unknown Sumsub external id %s",
                                billingProfileUpdated.getBillingProfileId()))));
    }

    private OldIndividualBillingProfile updateIndividualProfile(final BillingProfileUpdated billingProfileUpdated) {
        return oldBillingProfileStoragePort.saveIndividualProfile(
                oldBillingProfileStoragePort.findIndividualProfileById(billingProfileUpdated.getBillingProfileId())
                        .map(individualBillingProfile -> individualBillingProfile.toBuilder()
                                .reviewMessageForApplicant(billingProfileUpdated.getReviewMessageForApplicant())
                                .externalApplicantId(billingProfileUpdated.getExternalApplicantId())
                                .status(billingProfileUpdated.getOldVerificationStatus()).build())
                        .map(userVerificationStoragePort::updateIndividualVerification)
                        .orElseThrow(() -> new OutboxSkippingException(String.format("Skipping unknown Sumsub external id %s",
                                billingProfileUpdated.getBillingProfileId()))));
    }

    private BillingProfileUpdated updateBillingProfileEventWithUserData(final BillingProfileUpdated billingProfileUpdated) {
        return userStoragePort.getUserById(billingProfileUpdated.getUserId())
                .map(user -> billingProfileUpdated.toBuilder()
                        .githubUserId(user.getGithubUserId())
                        .githubUserEmail(user.getGithubEmail())
                        .githubUserId(user.getGithubUserId())
                        .githubAvatarUrl(user.getGithubAvatarUrl())
                        .githubLogin(user.getGithubLogin())
                        .build())
                .orElseThrow(() -> new OutboxSkippingException("User %s not found".formatted(billingProfileUpdated.getUserId())));
    }
}
