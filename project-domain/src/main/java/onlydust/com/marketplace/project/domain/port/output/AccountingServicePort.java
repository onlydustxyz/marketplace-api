package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.math.BigDecimal;

public interface AccountingServicePort {

    void createReward(ProjectId projectId, RewardId rewardId, BigDecimal amount, CurrencyView.Id currencyId);

    void cancelReward(RewardId rewardId, CurrencyView.Id currencyId);
}
