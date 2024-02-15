package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.job.OutboxConsumer;
import onlydust.com.marketplace.api.domain.job.OutboxSkippingException;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.input.AccountingUserObserverPort;
import onlydust.com.marketplace.api.domain.port.input.UserVerificationFacadePort;
import onlydust.com.marketplace.api.domain.port.output.*;

import java.util.UUID;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class UserVerificationService implements UserVerificationFacadePort, OutboxConsumer {

    private final OutboxPort outboxPort;
    private final Function<Event, BillingProfileUpdated> billingProfileExternalMapper;
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final UserVerificationStoragePort userVerificationStoragePort;
    private final AccountingUserObserverPort userObserver;
    private final NotificationPort notificationPort;
    private final UserStoragePort userStoragePort;

    @Override
    public void consumeUserVerificationEvent(Event event) {
        outboxPort.push(event);
    }

    @Override
    public void process(Event event) {
        final BillingProfileUpdated billingProfileUpdated = billingProfileExternalMapper.apply(event);
        final UUID userId = switch (billingProfileUpdated.getType()) {
            case COMPANY -> updateCompanyProfile(billingProfileUpdated).getUserId();
            case INDIVIDUAL -> updateIndividualProfile(billingProfileUpdated).getUserId();
        };
        userObserver.onBillingProfileUpdated(billingProfileUpdated);
        notifyNewVerificationEventProcessedForUser(userId, billingProfileUpdated);
    }

    private CompanyBillingProfile updateCompanyProfile(final BillingProfileUpdated billingProfileUpdated) {
        return billingProfileStoragePort.saveCompanyProfile(
                billingProfileStoragePort.findCompanyProfileById(billingProfileUpdated.getBillingProfileId())
                        .map(companyBillingProfile -> companyBillingProfile.toBuilder()
                                .status(billingProfileUpdated.getVerificationStatus())
                                .reviewMessageForApplicant(billingProfileUpdated.getReviewMessageForApplicant())
                                .build())
                        .map(userVerificationStoragePort::updateCompanyVerification)
                        .orElseThrow(() -> new OutboxSkippingException(String.format("Skipping unknown Sumsub external id %s",
                                billingProfileUpdated.getBillingProfileId()))));
    }

    private IndividualBillingProfile updateIndividualProfile(final BillingProfileUpdated billingProfileUpdated) {
        return billingProfileStoragePort.saveIndividualProfile(
                billingProfileStoragePort.findIndividualProfileById(billingProfileUpdated.getBillingProfileId())
                        .map(individualBillingProfile -> individualBillingProfile.toBuilder()
                                .reviewMessageForApplicant(billingProfileUpdated.getReviewMessageForApplicant())
                                .status(billingProfileUpdated.getVerificationStatus()).build())
                        .map(userVerificationStoragePort::updateIndividualVerification)
                        .orElseThrow(() -> new OutboxSkippingException(String.format("Skipping unknown Sumsub external id %s",
                                billingProfileUpdated.getBillingProfileId()))));
    }

    private void notifyNewVerificationEventProcessedForUser(final UUID userId, final BillingProfileUpdated billingProfileUpdated) {
        userStoragePort.getUserById(userId)
                .map(user -> billingProfileUpdated.toBuilder()
                        .userId(userId)
                        .githubUserId(user.getGithubUserId())
                        .githubUserEmail(user.getGithubEmail())
                        .githubUserId(user.getGithubUserId())
                        .githubAvatarUrl(user.getGithubAvatarUrl())
                        .githubLogin(user.getGithubLogin())
                        .build())
                .ifPresentOrElse(notificationPort::notifyNewVerificationEvent,
                        () -> LOGGER.warn("User %s not found, unable to notify new user " +
                                          "verification event"));
    }
}
