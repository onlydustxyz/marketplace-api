package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;

public class BillingProfileService implements BillingProfileFacadePort {
    @Override
    public InvoicePreview previewInvoice(UserId userId, BillingProfile.Id billingProfileId, List<RewardId> rewardIds) {
        return null; //new InvoicePreview();
    }

    @Override
    public Page<Invoice> getInvoicesForBillingProfile(UserId userId, BillingProfile.Id billingProfileId) {
        return Page.<Invoice>builder()
                .content(List.of())
                .totalPageNumber(0)
                .totalItemNumber(0)
                .build();
    }
}
