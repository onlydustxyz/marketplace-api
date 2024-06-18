package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GithubAppInstallationRepository extends JpaRepository<GithubAppInstallationViewEntity, Long> {

    List<GithubAppInstallationViewEntity> findAllByAccount_IdIn(final List<Long> ids);

    @Query("""
            SELECT i
            FROM GithubAppInstallationViewEntity i
            JOIN FETCH i.authorizedRepos r
            WHERE r.id.repoId = :repoId
            """)
    Optional<GithubAppInstallationViewEntity> findByRepoId(Long repoId);

//    Optional<GithubAppInstallationViewEntity> findByAuthorizedReposIdRepoIdContains(Long repoId);

}
