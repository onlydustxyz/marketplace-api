package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.accounting.SponsorAccountReadEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface SponsorAccountReadRepository extends Repository<SponsorAccountReadEntity, UUID> {
    List<SponsorAccountReadEntity> findAllBySponsorId(UUID sponsorId, Sort sort);
}
