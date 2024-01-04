package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

import java.util.List;
import java.util.UUID;

public interface RewardServicePort {

    UUID requestPayment(UUID requestorId, RequestRewardCommand requestRewardCommand);

    void cancelPayment(UUID rewardId);

    void markInvoiceAsReceived(List<UUID> rewardIds);
}
