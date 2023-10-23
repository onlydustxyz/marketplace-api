package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectToBudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectToBudgetIdRepository extends JpaRepository<ProjectToBudgetEntity, UUID> {
}
