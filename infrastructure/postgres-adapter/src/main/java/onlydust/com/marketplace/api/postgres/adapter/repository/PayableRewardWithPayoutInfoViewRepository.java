package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayableRewardWithPayoutInfoViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PayableRewardWithPayoutInfoViewRepository extends JpaRepository<PayableRewardWithPayoutInfoViewEntity, UUID> {
    @Query(nativeQuery = true, value = """
                        select pr.id                                  reward_id,
                               pr.amount,
                               pr.currency,
                               pr.usd_amount                          dollars_equivalent,
                               c.name                                 currency_name,
                               c.code                                 currency_code,
                               c.logo_url                             currency_logo_url,
                               eth_w.address                          ethereum_address,
                               strk_w.address                         starknet_address
                        from accounting.invoices i
                                 join payment_requests pr on pr.invoice_id = i.id
                                 join iam.users u on u.github_user_id = pr.recipient_id
                                 join currencies c on c.code = upper(cast(pr.currency as text))
                                 left join wallets eth_w on eth_w.user_id = u.id and eth_w.network = 'ethereum'
                                 left join wallets strk_w on strk_w.user_id = u.id and strk_w.network = 'starknet'
                                 left join payments r on r.request_id = pr.id
                        where i.id in (:invoiceIds) and r.id is null and i.status = 'APPROVED'
                        order by c.code,pr.amount
            """)
    List<PayableRewardWithPayoutInfoViewEntity> findAllByInvoiceIds(final List<UUID> invoiceIds);


    @Query(nativeQuery = true, value = """
                        select pr.id                                  reward_id,
                               pr.amount,
                               pr.currency,
                               pr.usd_amount                          dollars_equivalent,
                               c.name                                 currency_name,
                               c.code                                 currency_code,
                               c.logo_url                             currency_logo_url,
                               eth_w.address                          ethereum_address,
                               strk_w.address                         starknet_address
                        from reward_to_batch_payment rtbp
                                 join payment_requests pr on pr.id = rtbp.reward_id
                                 join accounting.invoices i on i.id = pr.invoice_id
                                 join iam.users u on u.github_user_id = pr.recipient_id
                                 join currencies c on c.code = upper(cast(pr.currency as text))
                                 left join wallets eth_w on eth_w.user_id = u.id and eth_w.network = 'ethereum'
                                 left join wallets strk_w on strk_w.user_id = u.id and strk_w.network = 'starknet'
                                 left join payments r on r.request_id = pr.id
                        where rtbp.batch_payment_id = :batchPaymentId and r.id is null and i.status = 'APPROVED'
                        order by c.code,pr.amount
            """)
    List<PayableRewardWithPayoutInfoViewEntity> findAllByBatchPaymentId(UUID batchPaymentId);
}
