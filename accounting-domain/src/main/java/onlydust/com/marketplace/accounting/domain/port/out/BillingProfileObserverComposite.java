package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

import java.util.List;

public class BillingProfileObserverComposite implements BillingProfileObserver {
    private final List<BillingProfileObserver> observers;

    public BillingProfileObserverComposite(BillingProfileObserver... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal) {
        observers.forEach(observer -> observer.onInvoiceUploaded(billingProfileId, invoiceId, isExternal));
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        observers.forEach(observer -> observer.onBillingProfileUpdated(billingProfileVerificationUpdated));
    }

    @Override
    public void onInvoiceRejected(@NonNull InvoiceRejected invoiceRejected) {
        observers.forEach(observer -> observer.onInvoiceRejected(invoiceRejected));
    }
}
