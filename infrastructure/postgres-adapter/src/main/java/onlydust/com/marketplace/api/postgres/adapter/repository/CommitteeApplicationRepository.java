package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitteeApplicationRepository extends JpaRepository<CommitteeApplicationEntity, CommitteeApplicationEntity.PrimaryKey> {
}
