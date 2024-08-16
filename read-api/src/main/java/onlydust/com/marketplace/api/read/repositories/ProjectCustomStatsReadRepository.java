package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectCustomStatReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface ProjectCustomStatsReadRepository extends Repository<ProjectCustomStatReadEntity, UUID> {
    @Query(value = """
            with contribution_stats as (select pgr.project_id                                                      as project_id,
                                               count(distinct c.id)
                                               filter ( where c.type = 'PULL_REQUEST' and c.status = 'COMPLETED' ) as merged_pr_count,
                                               count(distinct c.contributor_id)                                    as contributor_count
                                        from indexer_exp.contributions c
                                                 join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                                        where pgr.project_id = :projectId
                                          and (cast(:fromDate as text) is null or c.created_at >= :fromDate)
                                          and (cast(:toDate as text) is null or date_trunc('day', c.created_at) <= :toDate)
                                        group by pgr.project_id),
                 reward_stats as (select r.project_id         as project_id,
                                         count(distinct r.id) as reward_count
                                  from rewards r
                                  where r.project_id = :projectId
                                    and (cast(:fromDate as text) is null or r.requested_at >= :fromDate)
                                    and (cast(:toDate as text) is null or date_trunc('day', r.requested_at) <= :toDate)
                                  group by r.project_id)
            select coalesce(cs.project_id, rs.project_id) as project_id,
                   coalesce(cs.merged_pr_count, 0)        as merged_pr_count,
                   coalesce(cs.contributor_count, 0)      as active_contributor_count,
                   coalesce(rs.reward_count, 0)           as reward_count
            from contribution_stats cs
                     full join reward_stats rs on rs.project_id = cs.project_id
            """, nativeQuery = true)
    Optional<ProjectCustomStatReadEntity> findById(UUID projectId, Date fromDate, Date toDate);
}
