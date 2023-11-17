package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GithubAppInstallationRepository extends JpaRepository<GithubAppInstallationEntity, Long> {

    List<GithubAppInstallationEntity> findAllByAccount_IdIn(final List<Long> ids);

}
