package onlydust.com.marketplace.api.od.old.api.client.adapter;

import java.math.BigDecimal;
import java.util.UUID;

public interface HttpClientPort {

    UUID allocateBudget(UUID projectId, BigDecimal amount, String currency, UUID sponsorId);
}
