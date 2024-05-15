package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.TechnologyQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TechnologyViewEntityRepository extends JpaRepository<TechnologyQueryEntity, String> {

    @Query(value = """
            SELECT technology
            FROM project_technologies
            GROUP BY technology
            ORDER BY sum(line_count) DESC
            """, nativeQuery = true)
    List<TechnologyQueryEntity> findAcrossAllProjects();
}
