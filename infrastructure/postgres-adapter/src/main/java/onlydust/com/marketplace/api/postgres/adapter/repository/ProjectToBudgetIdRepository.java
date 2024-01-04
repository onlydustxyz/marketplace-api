package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectToBudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectToBudgetIdRepository extends JpaRepository<ProjectToBudgetEntity, UUID> {

}
