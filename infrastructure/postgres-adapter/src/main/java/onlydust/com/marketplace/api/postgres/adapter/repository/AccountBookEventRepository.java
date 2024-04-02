package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountBookEventRepository extends JpaRepository<AccountBookEventEntity, Long> {
    List<AccountBookEventEntity> findAllByAccountBookId(UUID accountBookId);

    List<AccountBookEventEntity> findAllByAccountBookIdAndIdGreaterThanEqualOrderByIdAsc(UUID accountBookId, Long eventId);

    Optional<AccountBookEventEntity> findFirstByAccountBookIdOrderByIdDesc(UUID accountBookId);
}
