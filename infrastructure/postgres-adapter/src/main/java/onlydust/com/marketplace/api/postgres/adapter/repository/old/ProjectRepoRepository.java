package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepoRepository extends JpaRepository<ProjectRepoEntity, ProjectRepoEntity.PrimaryKey> {
    @Query("SELECT EXISTS (SELECT 1 FROM ProjectRepoEntity p WHERE p.repoId = :repoId)")
    boolean existsByRepoId(Long repoId);

    List<ProjectRepoEntity> findAllByRepoId(Long repoId);
}
