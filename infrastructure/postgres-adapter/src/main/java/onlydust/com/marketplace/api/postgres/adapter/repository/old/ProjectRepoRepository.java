package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepoRepository extends JpaRepository<ProjectRepoEntity, ProjectRepoEntity.PrimaryKey> {
}
