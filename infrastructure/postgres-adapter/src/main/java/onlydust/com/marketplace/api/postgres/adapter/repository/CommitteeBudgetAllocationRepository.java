package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeBudgetAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitteeBudgetAllocationRepository extends JpaRepository<CommitteeBudgetAllocationEntity, CommitteeBudgetAllocationEntity.PrimaryKey> {
}
