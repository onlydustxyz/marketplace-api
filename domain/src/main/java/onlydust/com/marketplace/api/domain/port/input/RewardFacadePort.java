package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;

import java.util.UUID;

public interface RewardFacadePort {

    UUID requestPayment(UUID projectLeadId,
                        RequestRewardCommand requestRewardCommand);

    void cancelPayment(UUID projectLeadId, UUID projectId, UUID rewardId);

    void markInvoiceAsReceived(Long recipientId);
}
