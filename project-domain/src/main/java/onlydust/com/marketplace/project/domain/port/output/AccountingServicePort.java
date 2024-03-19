package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.view.CurrencyView;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountingServicePort {

    void createReward(UUID projectId, UUID rewardId, BigDecimal amount, CurrencyView.Id currencyId);

    void cancelReward(UUID rewardId, CurrencyView.Id currencyId);
}
