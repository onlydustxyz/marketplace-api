package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.GithubCommandEntity;
import onlydust.com.marketplace.kernel.model.Event;

public interface GithubCommandRepository extends OutboxRepository<GithubCommandEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new GithubCommandEntity(event));
    }

}
