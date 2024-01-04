package onlydust.com.marketplace.api.domain.port.output;

import java.util.List;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

public interface RewardServicePort {

  UUID requestPayment(UUID requestorId, RequestRewardCommand requestRewardCommand);

  void cancelPayment(UUID rewardId);

  void markInvoiceAsReceived(List<UUID> rewardIds);
}
