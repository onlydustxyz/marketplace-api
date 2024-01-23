package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HiddenContributorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HiddenContributorRepository extends JpaRepository<HiddenContributorEntity, HiddenContributorEntity.PrimaryKey> {
    Boolean existsByProjectIdAndProjectLeadId(UUID projectId, UUID projectLeadId);
}
