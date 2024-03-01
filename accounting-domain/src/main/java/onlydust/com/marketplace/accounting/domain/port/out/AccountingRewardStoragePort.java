package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.view.RewardView;

import java.util.List;

public interface AccountingRewardStoragePort {

    List<RewardView> searchRewards(List<Invoice.Status> statuses, List<Invoice.Id> invoiceIds);
}
