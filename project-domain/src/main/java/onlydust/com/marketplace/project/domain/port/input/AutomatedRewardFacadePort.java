package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.UserId;

import java.math.BigDecimal;

public interface AutomatedRewardFacadePort {

    void createOtherWorkAndReward(String projectSlug, UserId projectLeadId, String repositoryName, String reason, String recipientLogin, String currencyCode,
                                  BigDecimal amount);
}
