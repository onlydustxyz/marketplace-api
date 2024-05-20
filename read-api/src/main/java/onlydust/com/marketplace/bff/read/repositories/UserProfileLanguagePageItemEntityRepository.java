package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserProfileLanguagePageItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface UserProfileLanguagePageItemEntityRepository extends Repository<UserProfileLanguagePageItemEntity, UUID> {
    @Query(value = """
            select ulr.language_id                        as language_id,
                   ulr.rank                               as rank,
                   case
                       when ulr.rank_percentile < 0.33 THEN 'GREEN'
                       when ulr.rank_percentile < 0.66 THEN 'ORANGE'
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
            from users_languages_ranks ulr
                     left join contributions_stats_per_language_per_user stats
                          on stats.language_id = ulr.language_id and 
                             stats.contributor_id = ulr.contributor_id
                     left join received_rewards_stats_per_language_per_user reward_stats
                               on reward_stats.language_id = stats.language_id and 
                                  reward_stats.recipient_id = stats.contributor_id
            where ulr.contributor_id = :githubUserId
            """,
            countQuery = """
                    select ulr.language_id                      as language_id
                    from users_languages_ranks ulr
                    where ulr.contributor_id = :githubUserId
                    """,
            nativeQuery = true)
    Page<UserProfileLanguagePageItemEntity> findByContributorId(Long githubUserId, Pageable pageable);
}
