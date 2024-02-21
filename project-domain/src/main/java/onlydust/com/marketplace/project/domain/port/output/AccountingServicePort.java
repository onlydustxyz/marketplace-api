package onlydust.com.marketplace.project.domain.port.output;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountingServicePort {

    void createReward(UUID projectId, UUID rewardId, BigDecimal amount, String currencyCode);

    void cancelReward(UUID rewardId, String currencyCode);
}
