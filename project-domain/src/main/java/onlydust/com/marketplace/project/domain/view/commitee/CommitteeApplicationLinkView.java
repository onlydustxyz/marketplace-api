package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.project.domain.view.ProjectLeaderLinkView;
import onlydust.com.marketplace.project.domain.view.ProjectShortView;

import java.math.BigDecimal;

@Builder
public record CommitteeApplicationLinkView(@NonNull ProjectLeaderLinkView applicant, @NonNull ProjectShortView projectShortView, Integer score,
                                           BigDecimal allocatedBudget, CurrencyView currency) {
}
