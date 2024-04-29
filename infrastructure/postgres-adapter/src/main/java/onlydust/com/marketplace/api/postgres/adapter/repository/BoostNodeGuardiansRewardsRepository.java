package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BoostNodeGuardiansRewardsEventEntity;
import onlydust.com.marketplace.kernel.model.Event;

public interface BoostNodeGuardiansRewardsRepository extends OutboxRepository<BoostNodeGuardiansRewardsEventEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new BoostNodeGuardiansRewardsEventEntity(event));
    }
}
