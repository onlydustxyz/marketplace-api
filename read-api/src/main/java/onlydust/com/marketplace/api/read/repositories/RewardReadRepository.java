package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface RewardReadRepository extends JpaRepository<RewardReadEntity, UUID> {
    boolean existsByRecipientIdAndStatus_Status(Long githubUserId, RewardStatus.Input status);

    @Query("""
            SELECT r FROM RewardReadEntity r
            JOIN FETCH r.requestor
            JOIN FETCH r.recipient
            JOIN FETCH r.currency c
            JOIN FETCH r.project
            JOIN FETCH r.status s
            JOIN FETCH r.statusData sd
            LEFT JOIN FETCH r.invoice i
            LEFT JOIN FETCH i.billingProfile bp
            LEFT JOIN FETCH bp.kyc
            LEFT JOIN FETCH bp.kyb
            LEFT JOIN FETCH c.latestUsdQuote
            WHERE
                (:statuses IS NULL OR s.status IN :statuses) AND
                (:billingProfileIds IS NULL OR i.billingProfileId IN :billingProfileIds) AND
                (:recipientIds IS NULL OR r.recipientId IN :recipientIds) AND
                (CAST(:fromRequestedAt AS String) IS NULL OR r.requestedAt >= :fromRequestedAt) AND
                (CAST(:toRequestedAt AS String) IS NULL OR DATE_TRUNC('DAY', r.requestedAt) <= :toRequestedAt) AND
                (CAST(:fromProcessedAt AS String) IS NULL OR sd.paidAt >= :fromProcessedAt) AND
                (CAST(:toProcessedAt AS String) IS NULL OR DATE_TRUNC('DAY', sd.paidAt) <= :toProcessedAt)
            """)
    Page<RewardReadEntity> find(List<RewardStatus.Input> statuses,
                                List<UUID> billingProfileIds,
                                List<Long> recipientIds,
                                Date fromRequestedAt,
                                Date toRequestedAt,
                                Date fromProcessedAt,
                                Date toProcessedAt,
                                Pageable pageable);
}
