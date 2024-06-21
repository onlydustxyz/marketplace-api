package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.committee.CommitteeReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommitteeReadRepository extends JpaRepository<CommitteeReadEntity, UUID> {

}
