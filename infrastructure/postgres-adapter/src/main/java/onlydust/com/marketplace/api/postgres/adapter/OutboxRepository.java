package onlydust.com.marketplace.api.postgres.adapter;

import java.util.Optional;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OutboxRepository<E extends EventEntity> extends JpaRepository<E, Long> {

  void saveEvent(Event event);

  @Query(value = """
      SELECT next_notif
      FROM #{#entityName} next_notif
      WHERE next_notif.id = (SELECT min(n.id) FROM #{#entityName} n WHERE n.status = 'PENDING' OR n.status = 'FAILED')
      """)
  Optional<E> findNextToProcess();
}
