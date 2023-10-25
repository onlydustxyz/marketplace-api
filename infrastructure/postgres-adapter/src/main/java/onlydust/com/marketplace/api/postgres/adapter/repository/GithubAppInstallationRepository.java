package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubAppInstallationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubAppInstallationRepository extends JpaRepository<GithubAppInstallationEntity, Long> {
}
