package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ProjectLeadViewRepository extends JpaRepository<ProjectLeadViewEntity, UUID>, JpaSpecificationExecutor<ProjectLeadViewEntity> {

    List<ProjectLeadViewEntity> findAllByProjectId(UUID projectId);
}
