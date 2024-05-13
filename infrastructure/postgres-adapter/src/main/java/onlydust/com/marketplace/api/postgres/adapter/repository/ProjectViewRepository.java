package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProjectViewRepository extends JpaRepository<ProjectViewEntity, UUID>,
        JpaSpecificationExecutor<ProjectViewEntity> {

    Optional<ProjectViewEntity> findBySlug(String slug);


}
