package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectTechnologiesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ProjectTechnologiesRepository extends JpaRepository<ProjectTechnologiesEntity, UUID>,
        JpaSpecificationExecutor<ProjectTechnologiesEntity> {
}
