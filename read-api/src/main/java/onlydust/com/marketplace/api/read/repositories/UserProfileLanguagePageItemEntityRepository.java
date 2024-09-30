package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.UserProfileLanguagePageItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface UserProfileLanguagePageItemEntityRepository extends Repository<UserProfileLanguagePageItemEntity, UUID> {
    @Query(value = """
            with user_languages as (select u.github_user_id                   as contributor_id,
                                           unnest(upi.preferred_language_ids) as language_id
                                    from user_profile_info upi
                                             join iam.users u on u.id = upi.id
                                    union
                                    select ulr.contributor_id as contributor_id,
                                           ulr.language_id    as language_id
                                    from users_languages_ranks ulr)
            select ul.language_id                                as language_id,
                   l.name                                        as language_name,
                   coalesce(ulr.rank, 0)                         as rank,
                   case
                       when ulr.rank_percentile < 0.33 THEN 'GREEN'
                       when ulr.rank_percentile < 0.66 THEN 'ORANGE'
                       ELSE 'RED'
                       END                                       as contributing_status,
                   coalesce(stats.contribution_count, 0)         as contribution_count,
                   coalesce(reward_stats.reward_count, 0)        as reward_count,
                   coalesce(reward_stats.usd_total, 0)           as total_earned_usd,
                   coalesce((select jsonb_agg(distinct jsonb_build_object(
                           'id', p.id,
                           'slug', p.slug,
                           'name', p.name,
                           'logoUrl', p.logo_url))
                    from projects p
                    where p.id = any (stats.project_ids)
                       or p.id = any (reward_stats.project_ids)), '[]') as projects
            from user_languages ul
                     join languages l on l.id = ul.language_id
                     left join users_languages_ranks ulr
                               on ulr.contributor_id = ul.contributor_id and ulr.language_id = ul.language_id
                     left join contributions_stats_per_language_per_user stats
                               on stats.language_id = ul.language_id and
                                  stats.contributor_id = ul.contributor_id
                     left join received_rewards_stats_per_language_per_user reward_stats
                               on reward_stats.language_id = ul.language_id and
                                  reward_stats.recipient_id = ul.contributor_id
            where ul.contributor_id = :githubUserId
            """,
            countQuery = """
                    with user_languages as (select u.github_user_id                   as contributor_id,
                                                   unnest(upi.preferred_language_ids) as language_id
                                            from user_profile_info upi
                                                     join iam.users u on u.id = upi.id
                                            union
                                            select ulr.contributor_id as contributor_id,
                                                   ulr.language_id    as language_id
                                            from users_languages_ranks ulr)
                    select count(ul.language_id)
                    from user_languages ul
                    where ul.contributor_id = :githubUserId
                    """,
            nativeQuery = true)
    Page<UserProfileLanguagePageItemEntity> findByContributorId(Long githubUserId, Pageable pageable);
}
