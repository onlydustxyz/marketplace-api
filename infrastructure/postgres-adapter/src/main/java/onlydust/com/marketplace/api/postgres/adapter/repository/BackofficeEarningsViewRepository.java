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
            SELECT
                1 as id,
                jsonb_agg(jsonb_build_object(
                   'rewardCount', reward_stats_per_currency.reward_count,
                   'amount', reward_stats_per_currency.total_amount,
                   'dollarsEquivalent', reward_stats_per_currency.total_dollars_equivalent,
                   'currency', jsonb_build_object(
                       'id', reward_stats_per_currency.currency_id,
                       'code', reward_stats_per_currency.currency_code,
                       'name', reward_stats_per_currency.currency_name,
                       'decimals', reward_stats_per_currency.currency_decimals,
                       'logoUrl', reward_stats_per_currency.currency_logo_url
                    )
                )) as earnings_per_currency
            FROM (
                SELECT count(r.id) as reward_count,
                                     sum(r.amount)  as total_amount,
                                     coalesce(sum(rsd.amount_usd_equivalent), 0)  as total_dollars_equivalent,
                                     c.id as currency_id,
                                     c.code as currency_code,
                                     c.name as currency_name,
                                     c.decimals as currency_decimals,
                                     c.logo_url as currency_logo_url
                FROM rewards r
                    JOIN accounting.reward_statuses rs on rs.reward_id = r.id
                    JOIN accounting.reward_status_data rsd on rsd.reward_id = r.id
                    JOIN currencies c on c.id = r.currency_id
                WHERE (coalesce(:statuses) is null or cast(rs.status as text) in (:statuses))
                  AND (coalesce(:recipientIds) is null or r.recipient_id in (:recipientIds))
                  AND (coalesce(:billingProfileIds) is null or r.billing_profile_id in (:billingProfileIds))
                  AND (coalesce(:projectIds) is null or r.project_id in (:projectIds))
                  AND (coalesce(:fromDate) is null or r.requested_at >= cast(cast(:fromDate as text) as timestamp))
                  AND (coalesce(:toDate)   is null or r.requested_at <= cast(cast(:toDate   as text) as timestamp))
                GROUP BY c.id
            ) reward_stats_per_currency
            """, nativeQuery = true)
    BoEarningsViewEntity getEarnings(@NonNull List<String> statuses,
                                     @NonNull List<Long> recipientIds,
                                     @NonNull List<UUID> billingProfileIds,
                                     @NonNull List<UUID> projectIds,
                                     Date fromDate, Date toDate);

}
