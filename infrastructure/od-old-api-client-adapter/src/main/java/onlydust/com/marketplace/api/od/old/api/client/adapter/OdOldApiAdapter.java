package onlydust.com.marketplace.api.od.old.api.client.adapter;

import onlydust.com.marketplace.api.domain.port.output.BudgetStoragePort;

import java.math.BigDecimal;
import java.util.UUID;

public class OdOldApiAdapter implements BudgetStoragePort {
    @Override
    public void allocate(UUID projectId, BigDecimal amount, UUID sponsorId) {

    }
}
