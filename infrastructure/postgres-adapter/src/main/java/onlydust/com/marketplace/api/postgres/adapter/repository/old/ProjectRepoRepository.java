package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import java.util.Collection;
import java.util.Set;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepoRepository extends JpaRepository<ProjectRepoEntity, ProjectRepoEntity.PrimaryKey> {

  @Query("SELECT pr FROM ProjectRepoEntity pr WHERE pr.repoId IN :repoIds")
  Set<ProjectRepoEntity> findAllByRepoId(Collection<Long> repoIds);
}
