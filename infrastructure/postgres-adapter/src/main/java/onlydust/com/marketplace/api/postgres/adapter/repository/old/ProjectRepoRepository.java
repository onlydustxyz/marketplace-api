package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepoRepository extends JpaRepository<ProjectRepoEntity, ProjectRepoEntity.PrimaryKey> {

    List<ProjectRepoEntity> findAllByRepoId(Long repoId);
}
