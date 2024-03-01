package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.view.RewardView;

import java.util.List;
import java.util.UUID;

public interface AccountingRewardPort {
    List<RewardView> searchForInvoiceIds(List<UUID> invoiceIds);
}
