package onlydust.com.marketplace.bff.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.bff.read.entities.user.UserShortEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserShortRepository extends JpaRepository<UserShortEntity, UUID> {

    @Query(value = """
            SELECT
                u.id,
                u.github_user_id,
                u.github_login as login,
                COALESCE(upi.avatar_url, ga.avatar_url, u.github_avatar_url) AS avatar_url,
                u.email,
                u.last_seen_at,
                u.created_at
            FROM
                iam.users u
                LEFT JOIN user_profile_info upi ON upi.id = u.id
                LEFT JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id
            WHERE
                :login IS NULL OR u.github_login ILIKE CONCAT('%',:login,'%')
            """,
            nativeQuery = true)
    @NotNull
    Page<UserShortEntity> findAll(final String login, final @NotNull Pageable pageable);


    @Query(value = """
            SELECT
                u.id,
                u.github_user_id,
                u.github_login as login,
                COALESCE(upi.avatar_url, ga.avatar_url, u.github_avatar_url) AS avatar_url,
                u.email,
                u.last_seen_at,
                u.created_at
            FROM hackathon_registrations hc
                JOIN iam.users u on u.id = hc.user_id
                LEFT JOIN user_profile_info upi ON upi.id = u.id
                LEFT JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id
            WHERE hc.hackathon_id = :hackathonId AND
                (:login IS NULL OR u.github_login ILIKE CONCAT('%',:login,'%'))
            """,
            nativeQuery = true)
    @NotNull
    Page<UserShortEntity> findAllRegisteredOnHackathon(final String login, @NonNull UUID hackathonId, final @NotNull Pageable pageable);


}
