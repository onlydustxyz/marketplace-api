package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.reward.FullRewardStatusReadEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface FullRewardStatusReadRepository extends JpaRepository<FullRewardStatusReadEntity, UUID> {

    @Query("""
            SELECT r FROM FullRewardStatusReadEntity r
            JOIN FETCH r.requestor
            JOIN FETCH r.recipient rcp
            JOIN FETCH r.currency c
            JOIN FETCH r.project p
            LEFT JOIN FETCH r.invoice i
            LEFT JOIN FETCH i.billingProfile bp
            LEFT JOIN FETCH c.latestUsdQuote
            WHERE
                (:statuses IS NULL OR r.status IN :statuses) AND
                (:billingProfileIds IS NULL OR i.billingProfileId IN :billingProfileIds) AND
                (:recipientIds IS NULL OR rcp.githubUserId IN :recipientIds) AND
                (:projectIds IS NULL OR p.id IN :projectIds) AND
                (CAST(:fromRequestedAt AS String) IS NULL OR r.requestedAt >= :fromRequestedAt) AND
                (CAST(:toRequestedAt AS String) IS NULL OR DATE_TRUNC('DAY', r.requestedAt) <= :toRequestedAt) AND
                (CAST(:fromProcessedAt AS String) IS NULL OR r.paidAt >= :fromProcessedAt) AND
                (CAST(:toProcessedAt AS String) IS NULL OR DATE_TRUNC('DAY', r.paidAt) <= :toProcessedAt)
            """)
    Page<FullRewardStatusReadEntity> find(List<RewardStatus.Input> statuses,
                                          List<UUID> billingProfileIds,
                                          List<Long> recipientIds,
                                          List<UUID> projectIds,
                                          ZonedDateTime fromRequestedAt,
                                          ZonedDateTime toRequestedAt,
                                          ZonedDateTime fromProcessedAt,
                                          ZonedDateTime toProcessedAt,
                                          Pageable pageable);
}
