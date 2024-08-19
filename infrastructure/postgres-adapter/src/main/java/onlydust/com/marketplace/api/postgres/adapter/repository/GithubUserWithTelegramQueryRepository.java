package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubUserWithTelegramQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface GithubUserWithTelegramQueryRepository extends JpaRepository<GithubUserWithTelegramQueryEntity, String> {

    @Query(value = """
            select u.github_login, ci.contact telegram
            from iam.users u
            left join contact_informations ci on ci.user_id = u.id and ci.channel = 'TELEGRAM'
            where u.id = :userId
            """, nativeQuery = true)
    Optional<GithubUserWithTelegramQueryEntity> findByUserId(UUID userId);
}
