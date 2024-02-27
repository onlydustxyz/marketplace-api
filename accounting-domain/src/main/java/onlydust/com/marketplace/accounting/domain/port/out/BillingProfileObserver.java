package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

public interface BillingProfileObserver {
    void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal);

    void onInvoiceRejected(Invoice.Id invoiceId);

    void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated);
}
