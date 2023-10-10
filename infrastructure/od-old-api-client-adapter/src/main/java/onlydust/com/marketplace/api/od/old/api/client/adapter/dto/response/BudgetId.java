package onlydust.com.marketplace.api.od.old.api.client.adapter.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class BudgetId {
    @JsonProperty("budget_id")
    UUID budgetId;
}
