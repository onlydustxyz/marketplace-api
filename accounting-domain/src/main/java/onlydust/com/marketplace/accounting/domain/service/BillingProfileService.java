package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.BillingProfileId;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreviewView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.UUID;

public class BillingProfileService implements BillingProfileFacadePort {
    @Override
    public InvoicePreviewView generateNextInvoicePreviewForUserAndRewards(UUID userId, List<UUID> rewardIds) {
        return null;
    }

    @Override
    public Page<Invoice> getInvoicesForBillingProfile(UserId userId, BillingProfileId billingProfileId) {
        return Page.<Invoice>builder()
                .content(List.of())
                .totalPageNumber(0)
                .totalItemNumber(0)
                .build();
    }
}
