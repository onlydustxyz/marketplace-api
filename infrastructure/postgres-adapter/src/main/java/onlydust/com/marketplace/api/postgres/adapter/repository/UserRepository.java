package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByGithubUserId(Long githubUserId);

    Collection<UserEntity> findAllByLastSeenAtAfter(Date since);

    @Modifying
    @Query(value = """
            INSERT INTO iam.users (id, github_user_id, github_avatar_url, email, github_login, roles)
                VALUES (
                        :#{#userEntity.id},
                        :#{#userEntity.githubUserId},
                        :#{#userEntity.githubAvatarUrl},
                        :#{#userEntity.githubEmail},
                        :#{#userEntity.githubLogin},
                        cast(:#{#userEntity.rolesAsPostgresArray} as iam.user_role[])
                )
                ON CONFLICT (github_user_id) DO NOTHING
            """, nativeQuery = true)
    void tryInsert(UserEntity userEntity);

    @Modifying
    @Query(value = """
            REFRESH MATERIALIZED VIEW CONCURRENTLY contributions_stats_per_user;
            REFRESH MATERIALIZED VIEW CONCURRENTLY contributions_stats_per_user_per_week;
            REFRESH MATERIALIZED VIEW CONCURRENTLY contributions_stats_per_ecosystem_per_user;
            REFRESH MATERIALIZED VIEW CONCURRENTLY contributions_stats_per_ecosystem_per_user_per_week;
            REFRESH MATERIALIZED VIEW CONCURRENTLY contributions_stats_per_language_per_user;
            REFRESH MATERIALIZED VIEW CONCURRENTLY received_rewards_stats_per_user;
            REFRESH MATERIALIZED VIEW CONCURRENTLY received_rewards_stats_per_user_per_week;
            REFRESH MATERIALIZED VIEW CONCURRENTLY received_rewards_stats_per_ecosystem_per_user;
            REFRESH MATERIALIZED VIEW CONCURRENTLY received_rewards_stats_per_ecosystem_per_user_per_week;
            REFRESH MATERIALIZED VIEW CONCURRENTLY received_rewards_stats_per_language_per_user;
            REFRESH MATERIALIZED VIEW CONCURRENTLY received_rewards_stats_per_project_per_user;
            REFRESH MATERIALIZED VIEW CONCURRENTLY global_users_ranks;
            """, nativeQuery = true)
    void refreshUsersRanksAndStats();

    @Modifying
    @Query(value = """
            INSERT INTO historical_user_ranks (github_user_id, rank, timestamp)
            SELECT github_user_id, rank, :date FROM global_users_ranks
            """, nativeQuery = true)
    void historizeGlobalUsersRanks(Date date);
}
