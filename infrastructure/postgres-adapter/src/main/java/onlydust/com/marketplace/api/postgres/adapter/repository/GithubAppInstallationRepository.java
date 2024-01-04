package onlydust.com.marketplace.api.postgres.adapter.repository;


import java.util.List;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubAppInstallationRepository extends JpaRepository<GithubAppInstallationEntity, Long> {

  List<GithubAppInstallationEntity> findAllByAccount_IdIn(final List<Long> ids);

}
