package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.BillingProfileId;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreviewView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.UUID;

public interface BillingProfileFacadePort {
    InvoicePreviewView generateNextInvoicePreviewForUserAndRewards(UUID userId, List<UUID> rewardIds);

    Page<Invoice> getInvoicesForBillingProfile(UserId userId, BillingProfileId billingProfileId);
}
