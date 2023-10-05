package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.WorkItemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WorkItemIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemRepository extends JpaRepository<WorkItemEntity, WorkItemIdEntity> {
}
