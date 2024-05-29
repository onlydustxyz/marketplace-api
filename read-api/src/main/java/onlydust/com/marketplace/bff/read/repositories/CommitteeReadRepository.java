package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.committee.CommitteeReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommitteeReadRepository extends JpaRepository<CommitteeReadEntity, UUID> {

}
