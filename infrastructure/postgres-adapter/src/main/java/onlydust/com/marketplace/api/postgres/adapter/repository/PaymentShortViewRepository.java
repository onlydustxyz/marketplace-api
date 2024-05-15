package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.PaymentShortQueryEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PaymentShortViewRepository extends JpaRepository<PaymentShortQueryEntity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
            SELECT
                bp.id,
                bp.tech_created_at as created_at,
                bp.status,
                bp.network,
                sum(reward_stats_per_currency.reward_count) as reward_count,
                (select jsonb_agg(jsonb_build_object(
                                   'amount', reward_stats_per_currency.total_amount,
                                   'dollarsEquivalent', reward_stats_per_currency.total_dollars_equivalent,
                                   'currency', jsonb_build_object(
                                       'id', reward_stats_per_currency.currency_id,
                                       'code', reward_stats_per_currency.currency_code,
                                       'name', reward_stats_per_currency.currency_name,
                                       'decimals', reward_stats_per_currency.currency_decimals,
                                       'latestUsdQuote', reward_stats_per_currency.currency_latest_usd_quote,
                                       'logoUrl', reward_stats_per_currency.currency_logo_url
                                    )
                                )))   as totals_per_currency
            FROM accounting.batch_payments bp
                LEFT JOIN LATERAL (select count(r.id) as reward_count,
                                 sum(r.amount)  as total_amount,
                                 coalesce(sum(rsd.amount_usd_equivalent), 0)  as total_dollars_equivalent,
                                 c.id as currency_id,
                                 c.code as currency_code,
                                 c.name as currency_name,
                                 c.decimals as currency_decimals,
                                 luq.price as currency_latest_usd_quote,
                                 c.logo_url as currency_logo_url
                          from rewards r
                          join accounting.reward_status_data rsd on rsd.reward_id = r.id
                          join currencies c on c.id = r.currency_id
                          left join accounting.latest_usd_quotes luq on luq.currency_id = c.id
                          join accounting.batch_payment_rewards bpr on r.id = bpr.reward_id
                          where bpr.batch_payment_id = bp.id
                          group by c.id, luq.price)    reward_stats_per_currency ON true
            """;


    @Language("PostgreSQL")
    String GROUP_BY = """
            GROUP BY bp.id, bp.tech_created_at, bp.status, bp.network
            """;

    @Query(value = SELECT + """
            WHERE coalesce(:statuses) IS NULL OR cast(bp.status as text) IN (:statuses)
            """ + GROUP_BY, nativeQuery = true)
    Page<PaymentShortQueryEntity> findByStatuses(Set<String> statuses, Pageable pageable);

    @Query(value = SELECT + """
            WHERE coalesce(:ids) IS NULL OR bp.id IN (:ids)
            """ + GROUP_BY, nativeQuery = true)
    List<PaymentShortQueryEntity> findByIds(Set<UUID> ids);
}
