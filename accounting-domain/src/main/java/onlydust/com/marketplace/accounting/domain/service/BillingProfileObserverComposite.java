package onlydust.com.marketplace.accounting.domain.service;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserverPort;

import java.util.List;

public class BillingProfileObserverComposite implements BillingProfileObserverPort {
    private final List<BillingProfileObserverPort> observers;

    public BillingProfileObserverComposite(BillingProfileObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal) {
        observers.forEach(o -> o.onInvoiceUploaded(billingProfileId, invoiceId, isExternal));
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        observers.forEach(o -> o.onBillingProfileUpdated(billingProfileVerificationUpdated));
    }

    @Override
    public void onInvoiceRejected(final @NonNull Invoice.Id invoiceId, final @NonNull String reason) {
        observers.forEach(o -> o.onInvoiceRejected(invoiceId, reason));
    }
}
