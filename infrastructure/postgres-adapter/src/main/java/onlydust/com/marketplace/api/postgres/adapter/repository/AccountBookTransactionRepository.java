package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBookTransactionRepository extends JpaRepository<AccountBookTransactionEntity, Long> {
}