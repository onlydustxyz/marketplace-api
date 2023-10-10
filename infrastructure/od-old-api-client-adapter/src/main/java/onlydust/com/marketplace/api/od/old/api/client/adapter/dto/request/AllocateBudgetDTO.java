package onlydust.com.marketplace.api.od.old.api.client.adapter.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
public class AllocateBudgetDTO {
    BigDecimal amount;
    String currency;
    UUID sponsorId;
}
