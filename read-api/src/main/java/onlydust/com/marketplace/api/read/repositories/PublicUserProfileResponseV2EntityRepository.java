package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.PublicUserProfileResponseV2Entity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface PublicUserProfileResponseV2EntityRepository extends Repository<PublicUserProfileResponseV2Entity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
            select
                u.github_user_id                                                                                as github_user_id,
                u.user_id                                                                                       as user_id,
                u.login                                                                                         as login,
                u.avatar_url                                                                                    as avatar_url,
                u.email                                                                                         as email,
                gur.rank                                                                                        as rank,
                gur.rank_percentile                                                                             as rank_percentile,
                case
                    when gur.rank_percentile <= 0.02 then 'A'
                    when gur.rank_percentile <= 0.04 then 'B'
                    when gur.rank_percentile <= 0.06 then 'C'
                    when gur.rank_percentile <= 0.08 then 'D'
                    when gur.rank_percentile <= 0.10 then 'E'
                    else 'F'
                end                                                                                             as rank_category,
                coalesce(user_ecosystems.ecosystems, '[]')                                                      as ecosystems,
                gur.contribution_count                                                                          as contribution_count,
                gur.contributed_project_count                                                                   as contributed_project_count,
                gur.leaded_project_count                                                                        as leaded_project_count,
                gur.reward_count                                                                                as reward_count
            from iam.all_users u
                left join global_users_ranks gur on gur.github_user_id = u.github_user_id
                left join lateral (select jsonb_agg(distinct jsonb_build_object(
                                        'id', e.id,
                                        'name', e.name,
                                        'url', e.url,
                                        'logoUrl', e.logo_url,
                                        'bannerUrl', e.banner_url,
                                        'slug', e.slug
                                        )) as ecosystems
                                   from ecosystems e
                                   where e.id in (select stats_pe.ecosystem_id
                                                  from contributions_stats_per_ecosystem_per_user stats_pe
                                                  where stats_pe.contributor_id = gur.github_user_id
                                                  union
                                                  select reward_stats_pe.ecosystem_id
                                                  from received_rewards_stats_per_ecosystem_per_user reward_stats_pe
                                                  where reward_stats_pe.recipient_id = gur.github_user_id)
                                   ) user_ecosystems on true
            """;

    @Language("PostgreSQL")
    String WHERE_GITHUB_ID = """
            where
                u.github_user_id = :githubUserId
            """;

    @Language("PostgreSQL")
    String WHERE_GITHUB_LOGIN = """
            where
                u.login = :githubUserLogin
            """;

    @Query(value = SELECT + WHERE_GITHUB_ID, nativeQuery = true)
    Optional<PublicUserProfileResponseV2Entity> findByGithubUserId(Long githubUserId);

    @Query(value = SELECT + WHERE_GITHUB_LOGIN, nativeQuery = true)
    Optional<PublicUserProfileResponseV2Entity> findByGithubUserLogin(String githubUserLogin);
}
