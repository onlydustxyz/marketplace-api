package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountingMailEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectMailEventEntity;
import onlydust.com.marketplace.kernel.model.Event;

public interface ProjectMailEventRepository extends OutboxRepository<ProjectMailEventEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new ProjectMailEventEntity(event));
    }

}
