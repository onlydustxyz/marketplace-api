package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.view.InvoicePreviewView;

import java.util.List;
import java.util.UUID;

public interface InvoiceFacadePort {
    InvoicePreviewView generateNextInvoicePreviewForUserAndRewards(UUID id, List<UUID> rewardIds);
}
