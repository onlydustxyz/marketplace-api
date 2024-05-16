package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.PublicUserProfileResponseV2Entity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface PublicUserProfileResponseV2EntityRepository extends Repository<PublicUserProfileResponseV2Entity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
            select
                gur.github_user_id                                                                               as github_user_id,
                gur.rank                                                                                         as rank,
                gur.rank_percentile                                                                              as rank_percentile,
                case
                    when gur.rank_percentile < 0.1666 then 'A'
                    when gur.rank_percentile < 0.3333 then 'B'
                    when gur.rank_percentile < 0.4999 then 'C'
                    when gur.rank_percentile < 0.6666 then 'D'
                    when gur.rank_percentile < 0.8333 then 'E'
                    else 'F'
                end                                                                                              as rank_category,
                coalesce(jsonb_agg(distinct jsonb_build_object(
                                'id', e.id,
                                'name', e.name,
                                'url', e.url,
                                'logoUrl', e.logo_url,
                                'bannerUrl', e.banner_url
                                )) filter ( where pe.ecosystem_id is not null ), '[]')                           as ecosystems,
                sum(rc.completed_contribution_count)                                                                  as contribution_count,
                count(distinct pgr.project_id)                                                                   as contributed_project_count,
                count(distinct pgr.project_id) filter ( where pl.project_id is not null )                        as leaded_project_count,
                count(distinct r.id )                                                                            as reward_count
            from
                global_users_ranks gur
                left join iam.users u on u.github_user_id = gur.github_user_id
                left join indexer_exp.repos_contributors rc on rc.contributor_id = gur.github_user_id
                left join indexer_exp.github_repos gr on gr.id = rc.repo_id and gr.visibility = 'PUBLIC'
                left join project_github_repos pgr on pgr.github_repo_id = rc.repo_id
                left join projects_ecosystems pe on pe.project_id = pgr.project_id
                left join ecosystems e on e.id = pe.ecosystem_id
                left join projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
                left join project_leads pl on pl.project_id = pgr.project_id and pl.user_id = u.id
                left join rewards r on r.recipient_id = gur.github_user_id
            """;

    @Language("PostgreSQL")
    String WHERE_GITHUB_ID = """
            where
                gur.github_user_id = :githubUserId
            """;

    @Language("PostgreSQL")
    String WHERE_GITHUB_LOGIN = """
            where
                u.github_login = :githubUserLogin
            """;

    @Language("PostgreSQL")
    String GROUP_BY = """
            group by
                gur.github_user_id,
                gur.rank,
                gur.rank_percentile
            """;

    @Query(value = SELECT + WHERE_GITHUB_ID + GROUP_BY, nativeQuery = true)
    Optional<PublicUserProfileResponseV2Entity> findByGithubUserId(Long githubUserId);

    @Query(value = SELECT + WHERE_GITHUB_LOGIN + GROUP_BY, nativeQuery = true)
    Optional<PublicUserProfileResponseV2Entity> findByGithubUserLogin(String githubUserLogin);
}
