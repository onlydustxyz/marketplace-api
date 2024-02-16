package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

public interface ProjectAccountingObserver {
    void onAllowanceUpdated(ProjectId projectId, Currency.Id currencyId, PositiveAmount currentAllowance, PositiveAmount initialAllowance);

    void onBudgetAllocatedToProject(SponsorId from, ProjectId to);
}
