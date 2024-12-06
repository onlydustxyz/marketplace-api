package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProjectViewRepository extends JpaRepository<ProjectViewEntity, UUID>,
        JpaSpecificationExecutor<ProjectViewEntity> {

    Optional<ProjectViewEntity> findBySlug(String slug);

    @Query("""
            select p
            from ProjectViewEntity p
            where p.visibility = 'PUBLIC'
            order by p.rank desc nulls last
            """)
    Page<ProjectViewEntity> findAllOrderByRank(Pageable pageable);
}
