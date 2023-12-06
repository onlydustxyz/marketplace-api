package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProjectBudgetView {
    UUID id;
    UUID projectId;
    BigDecimal initialAmount;
    BigDecimal remainingAmount;
    BigDecimal spentAmount;
    BigDecimal initialAmountDollarsEquivalent;
    BigDecimal remainingAmountDollarsEquivalent;
    BigDecimal spentAmountDollarsEquivalent;
    Currency currency;
}
