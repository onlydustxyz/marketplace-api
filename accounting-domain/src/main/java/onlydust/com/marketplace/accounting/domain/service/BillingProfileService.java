package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreviewView;

import java.util.List;
import java.util.UUID;

public class BillingProfileService implements BillingProfileFacadePort
{
    @Override
    public InvoicePreviewView generateNextInvoicePreviewForUserAndRewards(UUID userId, List<UUID> rewardIds) {
        return null;
    }
}
