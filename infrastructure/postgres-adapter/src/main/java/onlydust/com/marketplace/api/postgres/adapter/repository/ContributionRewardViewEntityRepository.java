package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContributionRewardViewEntityRepository extends JpaRepository<ContributionRewardViewEntity, UUID> {

    @Query(value = """

            with payout_checks as (select u.github_user_id                           github_user_id,
                                         coalesce(wallets.list, '{}')               wallets,
                                         ba.iban is not null and ba.bic is not null has_bank_account
                                  from iam.users u
                                           left join public.user_payout_info upi on u.id = upi.user_id
                                           left join (select w.user_id, array_agg(distinct w.network) as list
                                                      from wallets w
                                                      group by w.user_id) wallets on wallets.user_id = u.id
                                           left join bank_accounts ba on ba.user_id = u.id),
                billing_profile_check as (select ubpt.user_id,
                                                 ubpt.billing_profile_type type,
                                                 (
                                                     case
                                                         when ubpt.billing_profile_type = 'INDIVIDUAL'
                                                             then ibp.verification_status = 'VERIFIED'
                                                         when ubpt.billing_profile_type = 'COMPANY'
                                                             then cbp.verification_status = 'VERIFIED'
                                                         else false
                                                         end
                                                     ) billing_profile_verified
                                          from user_billing_profile_types ubpt
                                                   left join individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                                                   left join company_billing_profiles cbp on cbp.user_id = ubpt.user_id)
           SELECT pr.id,
                  pr.requested_at,
                  r.processed_at,
                  pr.amount,
                  pr.currency,
                  CASE WHEN pr.currency = 'usd' THEN pr.amount else COALESCE(cuq.price, 0) * pr.amount END dollars_equivalent,
                  case
                      when r.id is not null then 'COMPLETE'
                      when not coalesce(bpc.billing_profile_verified, false) then 'PENDING_VERIFICATION'
                      when (case
                                when pr.currency in ('eth', 'lords', 'usdc')
                                    then not payout_checks.wallets @> array [cast('ethereum' as network)]
                                when pr.currency = 'strk' then not payout_checks.wallets @> array [cast('starknet' as network)]
                                when pr.currency = 'op' then not payout_checks.wallets @> array [cast('optimism' as network)]
                                when pr.currency = 'apt' then not payout_checks.wallets @> array [cast('aptos' as network)]
                                when pr.currency = 'usd' then not payout_checks.has_bank_account
                          end) then 'MISSING_PAYOUT_INFO'
                      when bpc.type = 'COMPANY' and pr.invoice_received_at is null then 'PENDING_INVOICE'
                      when pr.currency = 'strk' THEN 'LOCKED'
                      when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') THEN 'LOCKED'
                      else 'PROCESSING'
                      end                                             AS                                   status,
                  requestor.login                                     AS                                   requestor_login,
                  user_avatar_url(requestor.id, requestor.avatar_url) AS                                   requestor_avatar_url,
                  requestor.id                                        AS                                   requestor_id,
                  recipient.login                                     AS                                   recipient_login,
                  user_avatar_url(recipient.id, recipient.avatar_url) AS                                   recipient_avatar_url,
                  recipient.id                                        AS                                   recipient_id
           FROM indexer_exp.contributions c
                    JOIN public.work_items wi
                         ON wi.id = COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id)
                    JOIN payment_requests pr ON pr.id = wi.payment_id AND pr.recipient_id = c.contributor_id
                    JOIN iam.users u ON u.id = pr.requestor_id
                    JOIN indexer_exp.github_accounts requestor ON requestor.id = u.github_user_id
                    JOIN indexer_exp.github_accounts recipient ON recipient.id = pr.recipient_id
                    LEFT JOIN payments r ON r.request_id = pr.id
                    LEFT JOIN crypto_usd_quotes cuq ON cuq.currency = pr.currency
                    LEFT JOIN payout_checks ON payout_checks.github_user_id = pr.recipient_id
                    LEFT JOIN billing_profile_check bpc on bpc.user_id = u.id
           WHERE c.id = :contributionId
             AND pr.project_id = :projectId
            """, nativeQuery = true)
    List<ContributionRewardViewEntity> listByContributionId(UUID projectId, String contributionId);
}
