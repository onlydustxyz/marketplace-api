package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.project.domain.view.CurrencyView;

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
    CurrencyView currency;
}
