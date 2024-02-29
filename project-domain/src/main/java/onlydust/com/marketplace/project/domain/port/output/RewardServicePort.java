package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.OldPayRewardRequestCommand;
import onlydust.com.marketplace.project.domain.model.OldRequestRewardCommand;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RewardServicePort {

    UUID create(UUID requestorId, OldRequestRewardCommand oldRequestRewardCommand);

    void cancel(UUID rewardId);

    void markInvoiceAsReceived(List<UUID> rewardIds);

    void markPaymentAsReceived(BigDecimal amount, OldPayRewardRequestCommand oldPayRewardRequestCommand);
}
