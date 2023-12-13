package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.TechnologyViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TechnologyViewEntityRepository extends JpaRepository<TechnologyViewEntity, String> {

    @Query(value = """
            SELECT technology
            FROM project_technologies
            GROUP BY technology
            ORDER BY sum(line_count) DESC
            """, nativeQuery = true)
    List<TechnologyViewEntity> findAcrossAllProjects();
}
