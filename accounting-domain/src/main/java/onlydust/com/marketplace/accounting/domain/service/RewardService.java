package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.RewardView;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class RewardService implements AccountingRewardPort {

    private final AccountingRewardStoragePort accountingRewardStoragePort;
    @Override
    public List<RewardView> searchForInvoiceIds(List<UUID> invoiceIds) {
        return accountingRewardStoragePort.searchPayableRewardsForInvoiceIds(invoiceIds);
    }
}
