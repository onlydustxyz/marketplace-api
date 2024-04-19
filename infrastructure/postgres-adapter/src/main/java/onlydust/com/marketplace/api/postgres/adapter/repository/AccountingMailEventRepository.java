package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountingMailEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.TrackingEventEntity;
import onlydust.com.marketplace.kernel.model.Event;

public interface AccountingMailEventRepository extends OutboxRepository<AccountingMailEventEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new AccountingMailEventEntity(event));
    }

}
