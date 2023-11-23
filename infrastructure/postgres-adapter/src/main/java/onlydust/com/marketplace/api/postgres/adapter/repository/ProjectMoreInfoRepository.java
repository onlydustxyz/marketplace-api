package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectMoreInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMoreInfoRepository extends JpaRepository<ProjectMoreInfoEntity, ProjectMoreInfoEntity.Id> {
}
