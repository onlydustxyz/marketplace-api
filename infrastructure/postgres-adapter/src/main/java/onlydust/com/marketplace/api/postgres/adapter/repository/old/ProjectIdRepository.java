package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectIdRepository extends JpaRepository<ProjectIdEntity, UUID> {
}
