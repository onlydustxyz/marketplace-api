package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectCustomStatReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface ProjectCustomStatsReadRepository extends Repository<ProjectCustomStatReadEntity, UUID> {
    @Query(value = """
            select :projectId                                            as project_id,
                   coalesce(sum(cd.is_pr), 0)                            as merged_pr_count,
                   count(distinct cd.contributor_id)                     as active_contributor_count,
                   count(distinct cd.contributor_id)
                   filter ( where cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count
            from bi.p_per_contributor_contribution_data cd
            where :projectId = cd.project_id
              and (cast(:fromDate as text) is null or cd.timestamp >= :fromDate)
              and (cast(:toDate as text) is null or date_trunc('day', cd.timestamp) <= :toDate)
            """, nativeQuery = true)
    Optional<ProjectCustomStatReadEntity> findById(UUID projectId, Date fromDate, Date toDate);
}
