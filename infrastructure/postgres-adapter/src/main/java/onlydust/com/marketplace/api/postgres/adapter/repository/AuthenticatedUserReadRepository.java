package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.AuthenticatedUserReadEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticatedUserReadRepository extends Repository<AuthenticatedUserReadEntity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
            SELECT u.id                                                                            AS id,
                   COALESCE(ga.id, u.github_user_id)                                               AS github_user_id,
                   COALESCE(ga.login, u.github_login)                                              AS github_login,
                   COALESCE(upi.avatar_url, ga.avatar_url, u.github_avatar_url)                    AS github_avatar_url,
                   u.email                                                                         AS email,
                   u.roles                                                                         AS roles,
                   (select array_agg(pl.project_id) from project_leads pl where pl.user_id = u.id) AS project_led_ids,
                   (select jsonb_agg(jsonb_build_object(
                           'billingProfileId', bpu.billing_profile_id,
                           'role', bpu.role))
                    from accounting.billing_profiles_users bpu
                    where bpu.user_id = u.id)                                                      AS billing_profiles
            FROM iam.users u
                     LEFT JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id
                     LEFT JOIN user_profile_info upi ON upi.id = u.id
            """;

    @Query(value = SELECT + " WHERE u.id = :id", nativeQuery = true)
    Optional<AuthenticatedUserReadEntity> findById(UUID id);

    @Query(value = SELECT + " WHERE u.github_user_id = :githubId", nativeQuery = true)
    Optional<AuthenticatedUserReadEntity> findByGithubUserId(Long githubId);
}
