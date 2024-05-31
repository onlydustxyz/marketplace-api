package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.user.EcosystemContributorPageItemEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface EcosystemContributorPageItemEntityRepository extends Repository<EcosystemContributorPageItemEntity, UUID> {
    @Language("PostgreSQL")
    String SELECT = """
            select uer.ecosystem_id                       as ecosystem_id,
                   uer.contributor_id                     as contributor_id,
                   u.login                                as login,
                   u.avatar_url                           as avatar_url,
                   gur.rank                               as rank,
                   case
                        when gur.rank_percentile <= 0.02 then 'A'
                        when gur.rank_percentile <= 0.04 then 'B'
                        when gur.rank_percentile <= 0.06 then 'C'
                        when gur.rank_percentile <= 0.08 then 'D'
                        when gur.rank_percentile <= 0.10 then 'E'
                        else 'F'
                   end                                    as rank_category,
                   stats.contribution_count               as contribution_count,
                   rank() OVER (ORDER BY stats.contribution_count desc nulls last) as contribution_count_rank,
                   coalesce(reward_stats.reward_count, 0) as reward_count,
                   coalesce(reward_stats.usd_total, 0)    as total_earned_usd,
                   rank() OVER (ORDER BY reward_stats.usd_total desc nulls last) as total_earned_usd_rank
            from users_ecosystems_ranks uer
                     join global_users_ranks gur on gur.github_user_id = uer.contributor_id
                     join ecosystems eco on eco.id = uer.ecosystem_id
                     join iam.all_users u on u.github_user_id = uer.contributor_id
                     left join contributions_stats_per_ecosystem_per_user stats
                          on stats.ecosystem_id = uer.ecosystem_id and 
                             stats.contributor_id = uer.contributor_id
                     left join received_rewards_stats_per_ecosystem_per_user reward_stats
                               on reward_stats.ecosystem_id = uer.ecosystem_id and
                                  reward_stats.recipient_id = uer.contributor_id
            """;

    @Query(value = SELECT + """
            where eco.slug = :ecosystemSlug
            order by stats.contribution_count desc nulls last
            """,
            countQuery = """
                    select uer.ecosystem_id                     as ecosystem_id,
                           uer.contributor_id                   as contributor_id
                    from users_ecosystems_ranks uer
                            join ecosystems eco on eco.id = uer.ecosystem_id
                    where eco.slug = :ecosystemSlug
                    """,
            nativeQuery = true)
    Page<EcosystemContributorPageItemEntity> findByEcosystemSlugOrderByContributionCountDesc(String ecosystemSlug, Pageable pageable);

    @Query(value = SELECT + """
            where eco.slug = :ecosystemSlug
            order by reward_stats.usd_total desc nulls last
            """,
            countQuery = """
                    select uer.ecosystem_id                     as ecosystem_id,
                           uer.contributor_id                   as contributor_id
                    from users_ecosystems_ranks uer
                            join ecosystems eco on eco.id = uer.ecosystem_id
                    where eco.slug = :ecosystemSlug
                    """,
            nativeQuery = true)
    Page<EcosystemContributorPageItemEntity> findByEcosystemSlugOrderByTotalEarnedUsdDesc(String ecosystemSlug, Pageable pageable);
}
