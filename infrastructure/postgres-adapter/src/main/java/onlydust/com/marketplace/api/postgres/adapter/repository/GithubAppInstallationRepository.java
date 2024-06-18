package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GithubAppInstallationRepository extends JpaRepository<GithubAppInstallationViewEntity, Long> {

    List<GithubAppInstallationViewEntity> findAllByAccount_IdIn(final List<Long> ids);

    Optional<GithubAppInstallationViewEntity> findByAuthorizedReposIdRepoId(Long repoId);
}
