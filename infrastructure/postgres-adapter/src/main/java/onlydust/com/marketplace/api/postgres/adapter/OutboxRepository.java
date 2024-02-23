package onlydust.com.marketplace.api.postgres.adapter;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.EventEntity;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface OutboxRepository<E extends EventEntity> extends JpaRepository<E, Long> {

    void saveEvent(Event event);

    @Query(value = """
            SELECT next_event
            FROM #{#entityName} next_event
            WHERE next_event.id IN (SELECT min(n.id) FROM #{#entityName} n WHERE n.status = 'PENDING' OR n.status = 'FAILED' GROUP BY n.group)
            ORDER BY next_event.id
            """)
    List<E> findNextToProcess();
}
