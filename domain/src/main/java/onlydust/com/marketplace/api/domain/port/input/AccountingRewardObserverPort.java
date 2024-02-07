package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.Reward;

import java.util.UUID;

public interface AccountingRewardObserverPort {
    void onRewardCreated(Reward reward);

    void onRewardCancelled(UUID rewardId);

    void onPaymentRequested(UUID rewardId);
    
    void onInvoiceRejected(UUID rewardId);
}
