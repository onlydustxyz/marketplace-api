package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.model.Reward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardServicePort {

    UUID create(UUID requestorId, RequestRewardCommand requestRewardCommand);

    void cancel(UUID rewardId);

    void markInvoiceAsReceived(List<UUID> rewardIds);

    Optional<Reward> get(UUID id);
}
