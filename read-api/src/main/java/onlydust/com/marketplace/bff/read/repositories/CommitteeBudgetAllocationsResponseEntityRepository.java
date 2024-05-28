package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.CommitteeBudgetAllocationViewEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface CommitteeBudgetAllocationsResponseEntityRepository extends Repository<CommitteeBudgetAllocationViewEntity,
        CommitteeBudgetAllocationViewEntity.PrimaryKey> {
    List<CommitteeBudgetAllocationViewEntity> findAllByCommitteeId(UUID committeeId);
}
