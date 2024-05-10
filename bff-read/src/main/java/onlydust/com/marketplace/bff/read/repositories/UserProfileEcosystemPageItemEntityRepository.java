package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserProfileEcosystemPageItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface UserProfileEcosystemPageItemEntityRepository extends Repository<UserProfileEcosystemPageItemEntity, UUID> {
    @Query(value = """
            select pe.ecosystem_id                    as ecosystem_id,
                   uer.rank                           as rank,
                   case
                       when uer.rank_percentile < 0.33 THEN 'GREEN'
                       when uer.rank_percentile < 0.66 THEN 'ORANGE'
                       ELSE 'RED'
                       END                            as contributing_status,
                   count(distinct c.id)               as contribution_count,
                   count(distinct ri.reward_id)       as reward_count,
                   ROUND(sum(rewarded.usd_amount), 2) as total_earned_usd,
                   jsonb_agg(distinct jsonb_build_object(
                           'id', p.id,
                           'slug', p.key,
                           'name', p.name,
                           'logoUrl', p.logo_url
                                      ))              as projects
            from indexer_exp.contributions c
                     join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                     join projects_ecosystems pe on pe.project_id = pgr.project_id
                     join users_ecosystems_ranks uer on uer.ecosystem_id = pe.ecosystem_id and uer.contributor_id = c.contributor_id
                     join reward_items ri
                          on ri.id = coalesce(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id) and
                             ri.recipient_id = c.contributor_id
                     join projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
                     join lateral ( select distinct on (reward_id) amount_usd_equivalent as usd_amount
                                    from accounting.reward_status_data rsd
                                    where rsd.reward_id = ri.reward_id) rewarded on true
            where c.contributor_id = :githubUserId
            group by pe.ecosystem_id, uer.rank, uer.rank_percentile
            order by uer.rank_percentile
            """, nativeQuery = true)
    Page<UserProfileEcosystemPageItemEntity> findByContributorId(Long githubUserId, Pageable pageable);
}
