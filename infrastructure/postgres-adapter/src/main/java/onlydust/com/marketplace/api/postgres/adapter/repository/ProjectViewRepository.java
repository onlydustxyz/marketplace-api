package onlydust.com.marketplace.api.postgres.adapter.repository;


import java.util.Optional;
import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectViewRepository extends JpaRepository<ProjectViewEntity, UUID>,
    JpaSpecificationExecutor<ProjectViewEntity> {

  Optional<ProjectViewEntity> findByKey(String key);


}
