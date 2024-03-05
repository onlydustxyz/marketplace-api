package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BatchPaymentDetailsViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchPaymentDetailsViewRepository extends JpaRepository<BatchPaymentDetailsViewEntity, UUID> {


    @Query(nativeQuery = true, value = """
                select bp.id,
                       bp.transaction_hash,
                       bp.network,
                       bp.status,
                       bp.tech_created_at,
                       bp.csv,
                       reward.moneys,
                       reward.reward_ids
                from accounting.batch_payments bp
                join (select rtbp.batch_payment_id,
                             json_agg(pr.id) reward_ids,
                             json_agg(json_build_object('amount',pr.amount,'currencyName',c.name,
                             'currencyCode',c.code,'currencyLogoUrl',c.logo_url, 'dollarsEquivalent',pr.usd_amount)) moneys
                      from reward_to_batch_payment rtbp
                        join payment_requests pr on pr.id = rtbp.reward_id
                        join currencies c on c.code = upper(cast(pr.currency as text))
                        group by rtbp.batch_payment_id) reward on reward.batch_payment_id = bp.id
                where bp.status = 'PAID' order by bp.tech_created_at desc
            """)
    List<BatchPaymentDetailsViewEntity> findAllBy(final Integer pageIndex, final Integer pageSize);

    @Query(nativeQuery = true, value = """
                select bp.id,
                       bp.transaction_hash,
                       bp.network,
                       bp.status,
                       bp.tech_created_at,
                       bp.csv,
                       reward.moneys,
                       reward.reward_ids
                from accounting.batch_payments bp
                join (select rtbp.batch_payment_id,
                             json_agg(pr.id) reward_ids,
                             json_agg(json_build_object('amount',pr.amount,'currencyName',c.name,
                             'currencyCode',c.code,'currencyLogoUrl',c.logo_url, 'dollarsEquivalent',pr.usd_amount)) moneys
                      from reward_to_batch_payment rtbp
                        join payment_requests pr on pr.id = rtbp.reward_id
                        join currencies c on c.code = upper(cast(pr.currency as text))
                        group by rtbp.batch_payment_id) reward on reward.batch_payment_id = bp.id
                where bp.id = :batchPaymentId
            """)
    Optional<BatchPaymentDetailsViewEntity> findById(final UUID batchPaymentId);

    @Query(nativeQuery = true, value = """
                select count(*) from accounting.batch_payments where status = 'PAID'
            """)
    Long countAll();
}
