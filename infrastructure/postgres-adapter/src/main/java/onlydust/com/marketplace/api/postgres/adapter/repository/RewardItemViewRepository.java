package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardItemViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RewardItemViewRepository extends JpaRepository<RewardItemViewEntity, UUID> {
    @Query(value = """
                        
            """, nativeQuery = true)
    Page<RewardViewEntity> findAllByRewardId(UUID rewardId, Pageable pageable);
}
