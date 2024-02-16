package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.io.InputStream;
import java.util.List;

public interface BillingProfileFacadePort {
    InvoicePreview previewInvoice(UserId userId, BillingProfile.Id billingProfileId, List<RewardId> rewardIds);

    Page<Invoice> getInvoicesForBillingProfile(UserId userId, BillingProfile.Id billingProfileId);

    void uploadInvoice(UserId userId, BillingProfile.Id billingProfileId, Invoice.Id invoiceId, InputStream inputStream);
}
