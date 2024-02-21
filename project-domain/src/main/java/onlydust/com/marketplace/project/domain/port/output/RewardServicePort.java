package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;

import java.util.List;
import java.util.UUID;

public interface RewardServicePort {

    UUID create(UUID requestorId, RequestRewardCommand requestRewardCommand);

    void cancel(UUID rewardId);

    void markInvoiceAsReceived(List<UUID> rewardIds);
}
