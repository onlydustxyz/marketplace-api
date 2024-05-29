package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.committee.CommitteeBudgetAllocationReadEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface CommitteeBudgetAllocationsResponseEntityRepository extends Repository<CommitteeBudgetAllocationReadEntity,
        CommitteeBudgetAllocationReadEntity.PrimaryKey> {
    List<CommitteeBudgetAllocationReadEntity> findAllByCommitteeId(UUID committeeId);
}
