package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.user.UserProfileEcosystemPageItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface UserProfileEcosystemPageItemEntityRepository extends Repository<UserProfileEcosystemPageItemEntity, UUID> {
    @Query(value = """
            select uer.ecosystem_id                       as ecosystem_id,
                   uer.rank                               as rank,
                   case
                       when uer.rank_percentile < 0.33 THEN 'GREEN'
                       when uer.rank_percentile < 0.66 THEN 'ORANGE'
                       ELSE 'RED'
                       END                                as contributing_status,
                   stats.contribution_count               as contribution_count,
                   coalesce(reward_stats.reward_count, 0) as reward_count,
                   coalesce(reward_stats.usd_total, 0)    as total_earned_usd,
                   (select jsonb_agg(distinct jsonb_build_object(
                           'id', p.id,
                           'slug', p.slug,
                           'name', p.name,
                           'logoUrl', p.logo_url))
                    from projects p
                    where p.id = any (stats.project_ids)
                    or p.id = any (reward_stats.project_ids)) as projects
            from users_ecosystems_ranks uer 
                     left join contributions_stats_per_ecosystem_per_user stats
                          on stats.ecosystem_id = uer.ecosystem_id and 
                             stats.contributor_id = uer.contributor_id
                     left join received_rewards_stats_per_ecosystem_per_user reward_stats
                               on reward_stats.ecosystem_id = uer.ecosystem_id and
                                  reward_stats.recipient_id = uer.contributor_id
            where uer.contributor_id = :githubUserId
            """,
            countQuery = """
                    select uer.ecosystem_id                     as ecosystem_id
                    from users_ecosystems_ranks uer
                    where uer.contributor_id = :githubUserId
                    """,
            nativeQuery = true)
    Page<UserProfileEcosystemPageItemEntity> findByContributorId(Long githubUserId, Pageable pageable);
}
