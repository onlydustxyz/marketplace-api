package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardRepository extends JpaRepository<RewardEntity, UUID> {

    @Query(value = """
            select count(r.*) from rewards r
            join iam.users u on u.github_user_id = r.recipient_id and u.id = :userId
            where r.project_id = :projectId
            """, nativeQuery = true)
    Long countAllByRecipientIdAndProjectId(UUID userId, UUID projectId);

    List<RewardEntity> findAllByInvoiceId(UUID invoiceId);
}
