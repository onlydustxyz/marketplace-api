package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {

}
