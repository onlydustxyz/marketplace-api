package onlydust.com.marketplace.project.domain.port.input;

import java.math.BigDecimal;
import java.util.UUID;

public interface AutomatedRewardFacadePort {

    void createOtherWorkAndReward(String projectSlug, UUID projectLeadId, String repositoryName, String reason, String recipientLogin, String currencyCode,
                                  BigDecimal amount);
}
