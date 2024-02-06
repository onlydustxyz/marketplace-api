package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.job.OutboxConsumer;
import onlydust.com.marketplace.api.domain.job.OutboxSkippingException;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.input.UserVerificationFacadePort;
import onlydust.com.marketplace.api.domain.port.output.BillingProfileStoragePort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;
import onlydust.com.marketplace.api.domain.port.output.UserVerificationStoragePort;

import java.util.function.Function;

@AllArgsConstructor
public class UserVerificationService implements UserVerificationFacadePort, OutboxConsumer {

    private final OutboxPort outboxPort;
    private final Function<Event, BillingProfileUpdated> billingProfileExternalMapper;
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final UserVerificationStoragePort userVerificationStoragePort;

    @Override
    public void consumeUserVerificationEvent(Event event) {
        outboxPort.push(event);
    }

    @Override
    public void process(Event event) {
        final BillingProfileUpdated billingProfileUpdated = billingProfileExternalMapper.apply(event);
        switch (billingProfileUpdated.getType()) {
            case COMPANY -> updateCompanyProfile(billingProfileUpdated);
            case INDIVIDUAL -> updateIndividualProfile(billingProfileUpdated);
        }
        userObserver.onBillingProfileUpdated(billingProfileUpdated);
    }

    private void updateCompanyProfile(final BillingProfileUpdated billingProfileUpdated) {
        billingProfileStoragePort.saveCompanyProfile(
                billingProfileStoragePort.findCompanyProfileById(billingProfileUpdated.getBillingProfileId())
                        .map(companyBillingProfile -> companyBillingProfile.toBuilder().status(billingProfileUpdated.getVerificationStatus()).build())
                        .map(userVerificationStoragePort::updateCompanyVerification)
                        .orElseThrow(() -> new OutboxSkippingException(String.format("Skipping unknown Sumsub external id %s",
                                billingProfileUpdated.getBillingProfileId()))));
    }

    private void updateIndividualProfile(final BillingProfileUpdated billingProfileUpdated) {
        billingProfileStoragePort.saveIndividualProfile(
                billingProfileStoragePort.findIndividualProfileById(billingProfileUpdated.getBillingProfileId())
                        .map(individualBillingProfile -> individualBillingProfile.toBuilder().status(billingProfileUpdated.getVerificationStatus()).build())
                        .map(userVerificationStoragePort::updateIndividualVerification)
                        .orElseThrow(() -> new OutboxSkippingException(String.format("Skipping unknown Sumsub external id %s",
                                billingProfileUpdated.getBillingProfileId()))));
    }
}
