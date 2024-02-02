package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserVerificationEventEntity;

public interface UserVerificationEventRepository extends OutboxRepository<UserVerificationEventEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new UserVerificationEventEntity(event));
    }

}
