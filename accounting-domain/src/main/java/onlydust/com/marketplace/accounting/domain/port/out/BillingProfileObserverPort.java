package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;

public interface BillingProfileObserverPort {
    void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal);

    void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated);

    void onInvoiceRejected(final @NonNull Invoice.Id invoiceId, final @NonNull String rejectionReason);

    void onBillingProfileExternalVerificationRequested(final @NonNull BillingProfileChildrenKycVerification billingProfileChildrenKycVerification);
}
