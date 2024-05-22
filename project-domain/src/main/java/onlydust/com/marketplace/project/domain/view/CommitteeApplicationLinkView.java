package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.CurrencyView;

import java.math.BigDecimal;

@Builder
public record CommitteeApplicationLinkView(@NonNull ProjectLeaderLinkView applicant, @NonNull ProjectShortView projectShortView, Integer score,
                                           BigDecimal allocatedBudget, CurrencyView currency) {
}
