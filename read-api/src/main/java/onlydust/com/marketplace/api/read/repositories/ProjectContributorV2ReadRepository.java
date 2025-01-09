package onlydust.com.marketplace.api.read.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.read.entities.project.ProjectContributorV2ReadEntity;

public interface ProjectContributorV2ReadRepository extends JpaRepository<ProjectContributorV2ReadEntity, Long> {
    @Query(value = """
            select  c.contributor_id                                                                                                           as id,
                    c.contributor                                                                                                              as contributor,
                    coalesce(array_agg(cc.contribution_uuid order by cc.created_at) filter (where cc.contribution_status = 'COMPLETED'), '{}') as merged_pull_requests,
                    coalesce(rd.reward_ids, '{}')                                                                                              as rewards,
                    round(coalesce(sum(rd.total_usd_amount), 0), 2)                                                                            as total_earned_usd_amount
                from bi.p_per_contributor_contribution_data cc
                        join bi.p_contributor_global_data c on cc.contributor_id = c.contributor_id
                        left join lateral (select sum(usd_amount)                       as total_usd_amount,
                                                array_agg(reward_id order by timestamp) as reward_ids
                                            from bi.p_reward_data
                                            where contributor_id = cc.contributor_id
                                            and project_id = cc.project_id) rd on true
                where (cc.project_id = :projectId or cc.project_slug = :projectSlug)
                and (:search is null or c.contributor_login ilike '%' || :search || '%')
                group by c.contributor_id, rd.reward_ids
                order by cast(c.contributor ->> 'globalRank' as int)
            """, countQuery = """
                select count(distinct cc.contributor_id)
                from bi.p_per_contributor_contribution_data cc
                    join bi.p_contributor_global_data c on cc.contributor_id = c.contributor_id
                where (cc.project_id = :projectId or cc.project_slug = :projectSlug)
                and (:search is null or c.contributor_login ilike '%' || :search || '%')
            """, nativeQuery = true)
    Page<ProjectContributorV2ReadEntity> findAll(UUID projectId,
                                                 String projectSlug,
                                                 String search,
                                                 Pageable pageable);
}
