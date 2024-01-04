package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLeadRepository extends JpaRepository<ProjectLeadEntity, ProjectLeadEntity.PrimaryKey> {

}
