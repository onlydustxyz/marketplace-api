package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.RewardDetailsViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface RewardViewRepository extends JpaRepository<RewardDetailsViewEntity, UUID> {


    @Query(value = """
            select pr.id                                    id,
                   reward_status.value                      status,
                   pr.requested_at                          requested_at,
                   r.processed_at                           processed_at,
                   g_urls.urls                              github_urls,
                   pd.project_id                            project_id,
                   pd.name                                  project_name,
                   pd.logo_url                              project_logo_url,
                   pd.short_description                     project_short_description,
                   pd.key                                   project_slug,
                   s2.s_list                                sponsors,
                   pr.usd_amount                            dollars_equivalent,
                   pr.amount                                amount,
                   c.name                                   currency_name,
                   c.code                                   currency_code,
                   c.logo_url                               currency_logo_url,
                   
                   ubpt.billing_profile_type                billing_profile_type,
                   case
                       when ubpt.billing_profile_type = 'INDIVIDUAL' then ibp.first_name || ' ' || ibp.last_name
                       when ubpt.billing_profile_type = 'COMPANY' then cbp.name
                       end                                  billing_profile_name,
                   coalesce(ibp.id, cbp.id)                 billing_profile_id,
                   coalesce(ibp.verification_status,
                            cbp.verification_status)        billing_profile_verification_status,
                   gu.login                           billing_profile_admin_login,
                   gu.avatar_url                      billing_profile_admin_avatar_url,
                   upi.first_name || ' ' || upi.last_name   billing_profile_admin_name,
                   u.email                                  billing_profile_admin_email,
                   i.id                                     invoice_id,
                   i.number                                 invoice_number,
                   i.status                                 invoice_status,
                   case
                        when r.receipt -> 'Ethereum' is not null then r.receipt -> 'Ethereum' ->> 'transaction_hash'
                        when r.receipt -> 'Optimism' is not null then r.receipt -> 'Optimism' ->> 'transaction_hash'
                        when r.receipt -> 'Aptos' is not null then r.receipt -> 'Aptos' ->> 'transaction_hash'
                        when r.receipt -> 'Starknet' is not null then r.receipt -> 'Starknet' ->> 'transaction_hash'
                        when r.receipt -> 'Sepa' is not null then r.receipt -> 'Sepa' ->> 'transaction_reference'
                   end                                      transaction_hash,
                   case
                        when r.receipt -> 'Ethereum' is not null then coalesce(r.receipt -> 'Ethereum' ->> 'recipient_ens', r.receipt -> 'Ethereum' ->> 'recipient_address')
                        when r.receipt -> 'Optimism' is not null then coalesce(r.receipt -> 'Optimism' ->> 'recipient_ens', r.receipt -> 'Optimism' ->> 'recipient_address')
                        when r.receipt -> 'Aptos' is not null then coalesce(r.receipt -> 'Aptos' ->> 'recipient_ens', r.receipt -> 'Aptos' ->> 'recipient_address')
                        when r.receipt -> 'Starknet' is not null then coalesce(r.receipt -> 'Starknet' ->> 'recipient_ens', r.receipt -> 'Starknet' ->> 'recipient_address')
                        when r.receipt -> 'Sepa' is not null then r.receipt -> 'Sepa' ->> 'recipient_iban'
                   end                                      paid_to
                   
            from payment_requests pr
            join currencies c on c.code = upper(cast(pr.currency as text))
            join project_details pd on pd.project_id = pr.project_id
            left join (select ps2.project_id, json_agg(json_build_object('name', s.name, 'logoUrl', s.logo_url)) s_list
                       from sponsors s
                       join projects_sponsors ps2 on ps2.sponsor_id = s.id
                       group by ps2.project_id) s2 on s2.project_id = pr.project_id
            left join iam.users u on u.github_user_id = pr.recipient_id
            left join indexer_exp.github_accounts gu on gu.id = pr.recipient_id
            left join user_profile_info upi on upi.id = u.id
            left join user_billing_profile_types ubpt on ubpt.user_id = u.id
            left join individual_billing_profiles ibp on ubpt.billing_profile_type = 'INDIVIDUAL' and ibp.user_id = ubpt.user_id
            left join company_billing_profiles cbp on ubpt.billing_profile_type = 'COMPANY' and cbp.user_id = ubpt.user_id
                        
            left join payments r on r.request_id = pr.id
            left join accounting.invoices i on i.id = pr.invoice_id and i.status != 'DRAFT'
            left join unlock_op_on_projects uoop on uoop.project_id = pr.project_id                        
            left join (select wi.payment_id, json_agg(coalesce(gpr.html_url, gcr.html_url, gi.html_url)) urls
                       from work_items wi
                       left join indexer_exp.github_pull_requests gpr on cast(gpr.id as text) = wi.id
                       left join indexer_exp.github_code_reviews gcr on gcr.id = wi.id
                       left join indexer_exp.github_issues gi on cast(gi.id as text) = wi.id
                       group by wi.payment_id) g_urls on g_urls.payment_id = pr.id
                       
            left join lateral (
                    with billing_profile_check as (select ubpt.user_id              as user_id,
                                                ubpt.billing_profile_type   as type,
                                                (
                                                  case
                                                      when ubpt.billing_profile_type = 'INDIVIDUAL'
                                                          then ibp.verification_status = 'VERIFIED'
                                                      when ubpt.billing_profile_type = 'COMPANY'
                                                          then cbp.verification_status = 'VERIFIED'
                                                      else false
                                                  end
                                                )                           as billing_profile_verified
                                            from user_billing_profile_types ubpt
                                            left join individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                                            left join company_billing_profiles cbp on cbp.user_id = ubpt.user_id),
                                            
                    payout_checks as (select u.github_user_id                           github_user_id,
                                          coalesce(wallets.list, '{}')               wallets,
                                          ba.iban is not null and ba.bic is not null has_bank_account
                                   from iam.users u
                                   left join public.user_payout_info upi on u.id = upi.user_id
                                   left join (select w.user_id, array_agg(distinct w.network) as list
                                              from wallets w
                                              group by w.user_id) wallets on wallets.user_id = u.id
                                   left join bank_accounts ba on ba.user_id = u.id)
                        
                    select case
                       when r.id is not null then 'COMPLETE'
                       when u.id is null then 'PENDING_SIGNUP'
                       when not coalesce(bpc.billing_profile_verified, false) then 'PENDING_VERIFICATION'
                       when (case
                                 when pr.currency in ('eth', 'lords', 'usdc')
                                     then not payout_checks.wallets @> array [cast('ethereum' as network)]
                                 when pr.currency = 'strk' then not payout_checks.wallets @> array [cast('starknet' as network)]
                                 when pr.currency = 'op' then not payout_checks.wallets @> array [cast('optimism' as network)]
                                 when pr.currency = 'apt' then not payout_checks.wallets @> array [cast('aptos' as network)]
                                 when pr.currency = 'usd' then not payout_checks.has_bank_account
                           end) then 'MISSING_PAYOUT_INFO'
                       when pr.currency = 'op' and now() < to_date('2024-08-23', 'YYYY-MM-DD') and uoop.project_id is null THEN 'LOCKED'
                       when coalesce(pr.invoice_received_at, i.created_at) is null then 'PENDING_INVOICE'
                       else 'PROCESSING'
                   end as value
                   from payout_checks
                   left join billing_profile_check bpc on bpc.user_id = u.id
                   where payout_checks.github_user_id = pr.recipient_id) as reward_status on true
                       
            where reward_status.value in (:statuses)
                and (coalesce(:fromRequestedAt) is null or pr.requested_at >= cast(cast(:fromRequestedAt as text) as timestamp))
                and (coalesce(:toRequestedAt)   is null or pr.requested_at <= cast(cast(:toRequestedAt   as text) as timestamp))
                and (coalesce(:fromProcessedAt) is null or r.processed_at  >= cast(cast(:fromProcessedAt as text) as timestamp))
                and (coalesce(:toProcessedAt)   is null or r.processed_at  <= cast(cast(:toProcessedAt   as text) as timestamp))
            """, nativeQuery = true)
    Page<RewardDetailsViewEntity> findAllByStatusesAndDates(@NonNull List<String> statuses,
                                                            Date fromRequestedAt, Date toRequestedAt,
                                                            Date fromProcessedAt, Date toProcessedAt,
                                                            Pageable pageable);

    @Modifying
    @Query(nativeQuery = true, value = """
            update payment_requests
            set payment_notified_at = now()
            where id in (:rewardIds)
            """)
    void markRewardAsPaymentNotified(List<UUID> rewardIds);
}
