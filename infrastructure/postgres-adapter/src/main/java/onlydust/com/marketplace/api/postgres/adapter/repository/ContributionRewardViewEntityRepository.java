package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContributionRewardViewEntityRepository extends JpaRepository<ContributionRewardViewEntity, UUID> {

    @Query(value = """
                with payout_checks as (select u.github_user_id                                                            github_user_id,
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
                                             coalesce(wallets.list, '{}')                                                 wallets,
                                             ba.iban is not null and ba.bic is not null                                   has_bank_account
                                       from iam.users u
                                                left join public.user_payout_info upi on u.id = upi.user_id
                                                left join (select w.user_id, array_agg(distinct w.network) as list
                                                           from wallets w
                                                           group by w.user_id) wallets on wallets.user_id = u.id
                                                left join bank_accounts ba on ba.user_id = u.id)
            SELECT
                pr.id,
                pr.requested_at,
                r.processed_at,
                pr.amount,
                pr.currency,
                CASE WHEN pr.currency = 'usd' THEN pr.amount else COALESCE(cuq.price, 0) * pr.amount END dollars_equivalent,
                case
                   when r.id is not null then 'COMPLETE'
                   when pr.currency = 'strk' THEN 'LOCKED'
                   when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') THEN 'LOCKED'
                   when not payout_checks.valid_location or
                        (not payout_checks.valid_company and not payout_checks.valid_person) or
                        (case
                             when pr.currency in ('eth','lords','usdc') then not payout_checks.wallets @> array[cast('ethereum' as network)]
                             when pr.currency = 'strk' then not payout_checks.wallets @> array[cast('starknet' as network)]
                             when pr.currency = 'op' then not payout_checks.wallets @> array[cast('optimism' as network)]
                             when pr.currency = 'apt' then not payout_checks.wallets @> array[cast('aptos' as network)]
                             when pr.currency = 'usd' then not payout_checks.has_bank_account
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
            JOIN iam.users u ON u.id = pr.requestor_id
            JOIN indexer_exp.github_accounts requestor ON requestor.id = u.github_user_id
            JOIN indexer_exp.github_accounts recipient ON recipient.id = pr.recipient_id
            LEFT JOIN payments r ON r.request_id = pr.id
            LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = pr.currency
            LEFT JOIN payout_checks ON payout_checks.github_user_id = pr.recipient_id
            WHERE
                c.id = :contributionId AND
                pr.project_id = :projectId
            """, nativeQuery = true)
    List<ContributionRewardViewEntity> listByContributionId(UUID projectId, String contributionId);
}
