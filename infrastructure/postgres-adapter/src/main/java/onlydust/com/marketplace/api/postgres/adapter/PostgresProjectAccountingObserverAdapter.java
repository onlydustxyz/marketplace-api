package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.ProjectAccountingObserver;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectAllowanceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectAllowanceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectSponsorRepository;

import java.util.Date;

@AllArgsConstructor
public class PostgresProjectAccountingObserverAdapter implements ProjectAccountingObserver {

    private final ProjectAllowanceRepository projectAllowanceRepository;
    private final ProjectSponsorRepository projectSponsorRepository;

    @Override
    public void onAllowanceUpdated(ProjectId projectId, Currency.Id currencyId, PositiveAmount currentAllowance, PositiveAmount initialAllowance) {
        projectAllowanceRepository.save(new ProjectAllowanceEntity(projectId.value(), currencyId.value(), currentAllowance.getValue(),
                initialAllowance.getValue()));
    }

    @Override
    public void onBudgetAllocatedToProject(SponsorId from, ProjectId to) {
        projectSponsorRepository.save(new ProjectSponsorEntity(to.value(), from.value(), new Date()));
    }
}
