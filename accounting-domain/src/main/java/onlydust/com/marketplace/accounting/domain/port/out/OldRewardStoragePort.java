package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;

public interface OldRewardStoragePort {
    void markRewardAsPaid(PayableRewardWithPayoutInfoView payableRewardWithPayoutInfoView, String transactionHash);
}
