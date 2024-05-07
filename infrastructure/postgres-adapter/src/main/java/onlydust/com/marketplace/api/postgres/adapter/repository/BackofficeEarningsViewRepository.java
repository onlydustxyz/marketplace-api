package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoEarningsViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface BackofficeEarningsViewRepository extends JpaRepository<BoEarningsViewEntity, Integer> {

    @Query(value = """
            SELECT  r.currency_id as currency_id,
                    count(r.id) as reward_count,
                    sum(r.amount)  as total_amount,
                    coalesce(sum(rsd.amount_usd_equivalent), 0)  as total_dollars_equivalent
            FROM rewards r
                JOIN accounting.reward_statuses rs on rs.reward_id = r.id
                JOIN accounting.reward_status_data rsd on rsd.reward_id = r.id
            WHERE (coalesce(:statuses) is null or cast(rs.status as text) in (:statuses))
              AND (coalesce(:recipientIds) is null or r.recipient_id in (:recipientIds))
              AND (coalesce(:billingProfileIds) is null or r.billing_profile_id in (:billingProfileIds))
              AND (coalesce(:projectIds) is null or r.project_id in (:projectIds))
              AND (coalesce(:fromRequestedAt) is null or r.requested_at >= cast(cast(:fromRequestedAt as text) as timestamp))
              AND (coalesce(:toRequestedAt)   is null or r.requested_at <= cast(cast(:toRequestedAt   as text) as timestamp))
              AND (coalesce(:fromProcessedAt) is null or rsd.paid_at >= cast(cast(:fromProcessedAt as text) as timestamp))
              AND (coalesce(:toProcessedAt)   is null or rsd.paid_at <= cast(cast(:toProcessedAt   as text) as timestamp))
            GROUP BY r.currency_id
            """, nativeQuery = true)
    List<BoEarningsViewEntity> getEarnings(@NonNull List<String> statuses,
                                           @NonNull List<Long> recipientIds,
                                           @NonNull List<UUID> billingProfileIds,
                                           @NonNull List<UUID> projectIds,
                                           Date fromRequestedAt, Date toRequestedAt,
                                           Date fromProcessedAt, Date toProcessedAt);

}
