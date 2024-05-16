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
                gur.github_user_id                                                                              as github_user_id,
                gur.rank                                                                                        as rank,
                gur.rank_percentile                                                                             as rank_percentile,
                case
                    when gur.rank_percentile < 0.1666 then 'A'
                    when gur.rank_percentile < 0.3333 then 'B'
                    when gur.rank_percentile < 0.4999 then 'C'
                    when gur.rank_percentile < 0.6666 then 'D'
                    when gur.rank_percentile < 0.8333 then 'E'
                    else 'F'
                end                                                                                             as rank_category,
                coalesce(user_ecosystems.ecosystems, '[]')                                                      as ecosystems,
                gur.contribution_count                                                                          as contribution_count,
                gur.contributed_project_count                                                                   as contributed_project_count,
                gur.leaded_project_count                                                                        as leaded_project_count,
                gur.reward_count                                                                                as reward_count
            from
                global_users_ranks gur
                left join lateral (select jsonb_agg(distinct jsonb_build_object(
                                        'id', e.id,
                                        'name', e.name,
                                        'url', e.url,
                                        'logoUrl', e.logo_url,
                                        'bannerUrl', e.banner_url
                                        )) as ecosystems
                                   from ecosystems e
                                        join indexer_exp.repos_contributors rc on rc.contributor_id = gur.github_user_id
                                        join indexer_exp.github_repos gr on gr.id = rc.repo_id and gr.visibility = 'PUBLIC'
                                        join project_github_repos pgr on pgr.github_repo_id = gr.id
                                        join projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
                                        join projects_ecosystems pe on pe.ecosystem_id = e.id and pe.project_id = p.id) user_ecosystems on true
            """;

    @Language("PostgreSQL")
    String WHERE_GITHUB_ID = """
            where
                gur.github_user_id = :githubUserId
            """;

    @Language("PostgreSQL")
    String WHERE_GITHUB_LOGIN = """
                join indexer_exp.github_accounts ga on ga.id = gur.github_user_id
            where
                ga.login = :githubUserLogin
            """;

    @Query(value = SELECT + WHERE_GITHUB_ID, nativeQuery = true)
    Optional<PublicUserProfileResponseV2Entity> findByGithubUserId(Long githubUserId);

    @Query(value = SELECT + WHERE_GITHUB_LOGIN, nativeQuery = true)
    Optional<PublicUserProfileResponseV2Entity> findByGithubUserLogin(String githubUserLogin);
}
