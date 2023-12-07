package onlydust.com.marketplace.api.postgres.adapter;

import onlydust.com.marketplace.api.domain.model.notification.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OutboxRepository<E> extends JpaRepository<E, Long>, JpaSpecificationExecutor<E> {

    void saveEvent(Event event);

    Optional<E> findNextToProcess();
}
