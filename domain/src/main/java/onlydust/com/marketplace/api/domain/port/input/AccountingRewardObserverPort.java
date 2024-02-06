package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.Reward;

public interface AccountingRewardObserverPort {
    void onRewardCreated(Reward reward);

}
