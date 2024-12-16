package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BoostNodeGuardiansRewardsEventEntity;
import onlydust.com.marketplace.kernel.infrastructure.postgres.OutboxRepository;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoostNodeGuardiansRewardsRepository extends OutboxRepository<BoostNodeGuardiansRewardsEventEntity>,
        JpaRepository<BoostNodeGuardiansRewardsEventEntity, Long> {

    @Override
    default void saveEvent(@NonNull Event event) {
        save(new BoostNodeGuardiansRewardsEventEntity(event));
    }
}
