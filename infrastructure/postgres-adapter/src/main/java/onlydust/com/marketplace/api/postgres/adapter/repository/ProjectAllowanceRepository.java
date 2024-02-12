package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectAllowanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAllowanceRepository extends JpaRepository<ProjectAllowanceEntity, ProjectAllowanceEntity.PrimaryKey> {
}
