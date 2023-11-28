package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ProjectRepoRepository extends JpaRepository<ProjectRepoEntity, ProjectRepoEntity.PrimaryKey> {
    @Query("SELECT pr FROM ProjectRepoEntity pr WHERE pr.primaryKey.repoId IN :repoIds")
    Set<ProjectRepoEntity> findAllByRepoId(Collection<Long> repoIds);
}
