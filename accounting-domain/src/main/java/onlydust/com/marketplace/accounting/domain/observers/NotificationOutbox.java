package onlydust.com.marketplace.accounting.domain.observers;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.events.InvoiceUploaded;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserver;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;

@AllArgsConstructor
public class NotificationOutbox implements BillingProfileObserver {
    private final @NonNull OutboxPort notificationOutbox;

    @Override
    public void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal) {
        notificationOutbox.push(new InvoiceUploaded(billingProfileId, invoiceId, isExternal));
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {

    }
}
