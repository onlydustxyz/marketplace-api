package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountBookEventRepository extends JpaRepository<AccountBookEventEntity, Long> {
    List<AccountBookEventEntity> findAllByAccountBookId(UUID accountBookId);

    List<AccountBookEventEntity> findAllByAccountBookIdAndIdGreaterThanEqualOrderByIdAsc(UUID accountBookId, Long eventId);

    Optional<AccountBookEventEntity> findFirstByAccountBookIdOrderByIdDesc(UUID accountBookId);

    @Modifying
    @Query(value = """
            insert into accounting.account_books_events (id, account_book_id, timestamp, payload)
            values (:#{#accountBookEventEntity.id()}, :#{#accountBookEventEntity.accountBookId()}, :#{#accountBookEventEntity.timestamp()}, cast(:#{#accountBookEventEntity.payloadAsJsonString()} as jsonb))
            """, nativeQuery = true)
    void insert(AccountBookEventEntity accountBookEventEntity);
}
