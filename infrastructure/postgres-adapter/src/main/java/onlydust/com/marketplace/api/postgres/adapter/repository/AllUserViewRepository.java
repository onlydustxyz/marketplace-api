package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.AllUserViewEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface AllUserViewRepository extends Repository<AllUserViewEntity, Long> {
    Optional<AllUserViewEntity> findByGithubUserId(Long githubUserId);
}
