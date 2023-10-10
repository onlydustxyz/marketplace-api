package onlydust.com.marketplace.api.domain.port.output;

import java.math.BigDecimal;
import java.util.UUID;

public interface BudgetStoragePort {

    void allocate(final UUID projectId, final BigDecimal amount, final UUID sponsorId);
}
