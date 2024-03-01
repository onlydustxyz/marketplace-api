package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.view.RewardView;

import java.util.List;
import java.util.UUID;

public interface AccountingRewardStoragePort {

    List<RewardView> searchPayableRewardsForInvoiceIds(List<UUID> invoiceIds);
}
