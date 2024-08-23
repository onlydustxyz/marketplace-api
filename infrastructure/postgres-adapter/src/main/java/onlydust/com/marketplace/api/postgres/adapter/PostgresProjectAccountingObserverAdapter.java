package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.port.out.ProjectAccountingObserver;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectAllowanceEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectAllowanceRepository;

@AllArgsConstructor
public class PostgresProjectAccountingObserverAdapter implements ProjectAccountingObserver {

    private final ProjectAllowanceRepository projectAllowanceRepository;

    @Override
    public void onAllowanceUpdated(ProjectId projectId, Currency.Id currencyId, PositiveAmount currentAllowance, PositiveAmount initialAllowance) {
        projectAllowanceRepository.save(new ProjectAllowanceEntity(projectId.value(), currencyId.value(), currentAllowance.getValue(),
                initialAllowance.getValue()));
    }
}
