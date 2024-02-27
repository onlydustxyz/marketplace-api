package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.kernel.model.Event;

public interface BillingProfileVerificationFacadePort {

    void consumeBillingProfileVerificationEvent(final Event event);
}
