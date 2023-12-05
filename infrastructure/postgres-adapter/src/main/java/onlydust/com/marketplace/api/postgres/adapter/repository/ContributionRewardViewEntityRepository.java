package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContributionRewardViewEntityRepository extends JpaRepository<ContributionRewardViewEntity, UUID> {

    @Query(value = """
            with payout_checks as (
                select pr.id,
                      pr.recipient_id,
                      au.id                                                            user_id,
                      (select count(p.id) > 0
                       from payment_requests pr2
                                left join payments p on p.request_id = pr2.id
                       where p.id is null
                         and pr.id = pr2.id)                                           has_pending_payments,
                      (upi.identity is not null and upi.identity -> 'Person' is not null and
                     upi.identity -> 'Person' -> 'lastname' != cast('null' as jsonb) and
                     upi.identity -> 'Person' -> 'firstname' != cast('null' as jsonb))            valid_person,
                    (upi.location is not null and upi.location -> 'city' != cast('null' as jsonb) and
                     upi.location -> 'post_code' != cast('null' as jsonb) and
                     upi.location -> 'address' != cast('null' as jsonb) and
                     upi.location -> 'country' != cast('null' as jsonb))                          valid_location,
                    (upi.identity is not null and upi.identity -> 'Company' is not null and
                     upi.identity -> 'Company' -> 'name' != cast('null' as jsonb) and
                     upi.identity -> 'Company' -> 'identification_number' != cast('null' as jsonb) and
                     upi.identity -> 'Company' -> 'owner' is not null and
                     upi.identity -> 'Company' -> 'owner' -> 'firstname' != cast('null' as jsonb) and
                     upi.identity -> 'Company' -> 'owner' -> 'lastname' != cast('null' as jsonb)) valid_company,
                      coalesce((select w_eth.address is not null
                                from payment_requests pr_eth
                                         left join payments p_eth on p_eth.request_id = pr_eth.id
                                         left join wallets w_eth
                                                   on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                where pr_eth.currency = 'eth'
                                  and pr_eth.id = pr.id
                                  and pr_eth.recipient_id = au.github_user_id
                                  and p_eth is null
                                limit 1), true)                                        valid_eth_wallet,
                      coalesce((select w_op.address is not null
                                from payment_requests pr_op
                                         left join payments p_op on p_op.request_id = pr_op.id
                                         left join wallets w_op on w_op.user_id = upi.user_id and w_op.network = 'optimism'
                                where pr_op.currency = 'op'
                                  and pr_op.id = pr.id
                                  and pr_op.recipient_id = au.github_user_id
                                  and p_op is null
                                limit 1), true)                                        valid_op_wallet,
                      coalesce((select w_stark.address is not null
                                from payment_requests pr_stark
                                         left join payments p_stark on p_stark.request_id = pr_stark.id
                                         left join wallets w_stark
                                                   on w_stark.user_id = upi.user_id and w_stark.network = 'starknet'
                                where pr_stark.currency = 'stark'
                                  and pr_stark.id = pr.id
                                  and pr_stark.recipient_id = au.github_user_id
                                  and p_stark is null
                                limit 1), true)                                        valid_stark_wallet,
                      coalesce((select w_apt.address is not null
                                from payment_requests pr_apt
                                         left join payments p_apt on p_apt.request_id = pr_apt.id
                                         left join wallets w_apt on w_apt.user_id = upi.user_id and w_apt.network = 'aptos'
                                where pr_apt.currency = 'apt'
                                  and pr_apt.recipient_id = au.github_user_id
                                  and p_apt is null
                                limit 1), true)                                        valid_apt_wallet,
                      case
                          when (
                              (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'fiat' and
                               (select count(pr_usd.id) > 0
                                from payment_requests pr_usd
                                         left join payments p_usd on p_usd.request_id = pr_usd.id
                                where pr_usd.recipient_id = au.github_user_id
                                  and pr_usd.id = pr.id
                                  and pr_usd.currency = 'usd'
                                  and p_usd.id is null))
                              ) then (select count(*) > 0
                                      from bank_accounts ba
                                      where ba.user_id = upi.user_id)
                          else true end                                                valid_banking_account,
                      case
                          when (upi.identity -> 'Person' is not null) then (
                              coalesce((select w_eth.address is not null
                                        from payment_requests pr_usdc
                                                 left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                                 left join wallets w_eth
                                                           on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                        where pr_usdc.currency = 'usd'
                                          and pr_usdc.id = pr.id
                                          and pr_usdc.recipient_id = au.github_user_id
                                          and p_usdc is null
                                        limit 1), true)
                              )
                          when (upi.identity -> 'Company' is not null and upi.usd_preferred_method = 'crypto')
                              then (
                              coalesce((select w_eth.address is not null
                                        from payment_requests pr_usdc
                                                 left join payments p_usdc on p_usdc.request_id = pr_usdc.id
                                                 left join wallets w_eth
                                                           on w_eth.user_id = upi.user_id and w_eth.network = 'ethereum'
                                        where pr_usdc.currency = 'usd'
                                          and pr_usdc.id = pr.id
                                          and pr_usdc.recipient_id = au.github_user_id
                                          and p_usdc is null
                                        limit 1), true)
                              )
                          else true
                          end                                                          valid_usdc_wallet
               from payment_requests pr
                        left join auth_users au on au.github_user_id = pr.recipient_id
                        left join public.user_payout_info upi on au.id = upi.user_id)
            SELECT
                pr.id,
                pr.requested_at,
                r.processed_at,
                pr.amount,
                pr.currency,
                CASE WHEN pr.currency = 'usd' THEN pr.amount else COALESCE(cuq.price, 0) * pr.amount END dollars_equivalent,
                case
                   when r.id is not null then 'COMPLETE'
                   when (case
                             when pr.currency = 'eth' then
                                     not payout_checks.valid_location or
                                     (not payout_checks.valid_company and not payout_checks.valid_person) or
                                     not payout_checks.valid_eth_wallet
                             when pr.currency = 'stark' then
                                     not payout_checks.valid_location or
                                     (not payout_checks.valid_company and not payout_checks.valid_person) or
                                     not payout_checks.valid_stark_wallet
                             when pr.currency = 'op' then
                                     not payout_checks.valid_location or
                                     (not payout_checks.valid_company and not payout_checks.valid_person) or
                                     not payout_checks.valid_op_wallet
                             when pr.currency = 'apt' then
                                     not payout_checks.valid_location or
                                     (not payout_checks.valid_company and not payout_checks.valid_person) or
                                     not payout_checks.valid_apt_wallet
                             when pr.currency = 'usd' then (
                                     not payout_checks.valid_location or
                                     (not payout_checks.valid_company and not payout_checks.valid_person)
                                     or (not payout_checks.valid_usdc_wallet or not payout_checks.valid_banking_account)
                                 )
                       end) then 'MISSING_PAYOUT_INFO'
                   when payout_checks.valid_company and pr.invoice_received_at is null then 'PENDING_INVOICE'
                   else 'PROCESSING'
                   end AS status,
                requestor.login AS requestor_login,
                user_avatar_url(requestor.id, requestor.avatar_url) AS requestor_avatar_url,
                requestor.id AS requestor_id,
                recipient.login AS recipient_login,
                user_avatar_url(recipient.id, recipient.avatar_url) AS recipient_avatar_url,
                recipient.id AS recipient_id
            FROM
                indexer_exp.contributions c
            JOIN public.work_items wi ON wi.id = COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id)
            JOIN payment_requests pr ON pr.id = wi.payment_id AND pr.recipient_id = c.contributor_id
            JOIN auth_users au ON au.id = pr.requestor_id
            JOIN indexer_exp.github_accounts requestor ON requestor.id = au.github_user_id
            JOIN indexer_exp.github_accounts recipient ON recipient.id = pr.recipient_id
            LEFT JOIN payments r ON r.request_id = pr.id
            LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = pr.currency
            LEFT JOIN payout_checks ON payout_checks.recipient_id = pr.recipient_id AND payout_checks.id = pr.id
            WHERE
                c.id = :contributionId AND
                pr.project_id = :projectId
            """, nativeQuery = true)
    List<ContributionRewardViewEntity> listByContributionId(UUID projectId, String contributionId);
}
