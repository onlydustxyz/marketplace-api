package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.InvoiceRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InvoiceRewardViewRepository extends JpaRepository<InvoiceRewardViewEntity, UUID> {

    @Query(value = """
            select pr.id                                  reward_id,
                   pr.amount,
                   pr.requested_at,
                   pd.name                                project_name,
                   pd.logo_url                            project_logo_url,
                   s2.s_list                              sponsors,
                   r.processed_at,
                   u.github_login                         recipient_login,
                   u.github_avatar_url                    recipient_avatar_url,
                   u.email                                recipient_email,
                   upi.first_name || ' ' || upi.last_name recipient_name,
                   pr.currency,
                   g_urls.urls                            github_urls,
                   pr.usd_amount                          dollars_equivalent,
                   coalesce(ibp.id, cbp.id)               billing_profile_id,
                   ubpt.billing_profile_type,
                   case
                       when ubpt.billing_profile_type = 'INDIVIDUAL' then ibp.first_name || ' ' || ibp.last_name
                       when ubpt.billing_profile_type = 'COMPANY' then cbp.name
                       end                                billing_profile_name,
                   c.name                                 currency_name,
                   c.code                                 currency_code,
                   c.logo_url                             currency_logo_url,
                   case
                        when r.receipt -> 'Ethereum' is not null then r.receipt ->> '{Ethereum, transaction_hash}'
                        when r.receipt -> 'Optimism' is not null then r.receipt ->> '{Optimism, transaction_hash}'
                        when r.receipt -> 'Aptos' is not null then r.receipt ->> '{Aptos, transaction_hash}'
                        when r.receipt -> 'Starknet' is not null then r.receipt ->> '{Starknet, transaction_hash}'
                        when r.receipt -> 'Sepa' is not null then r.receipt ->> '{Sepa, transaction_reference}'
                        end                                transaction_hash
            from accounting.invoices i
                     join payment_requests pr on pr.invoice_id = i.id
                     join currencies c on c.code = upper(pr.currency::text)
                     join project_details pd on pr.project_id = pd.project_id
                     left join (select ps2.project_id, json_agg(json_build_object('name', s.name, 'logo_url', s.logo_url)) s_list
                                from sponsors s
                                         join projects_sponsors ps2 on ps2.sponsor_id = s.id
                                group by ps2.project_id) s2 on s2.project_id = pr.project_id
                     join iam.users u on u.github_user_id = pr.recipient_id
                     join user_profile_info upi on upi.id = u.id
                     join user_billing_profile_types ubpt on ubpt.user_id = u.id
                     left join individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                     left join company_billing_profiles cbp on cbp.user_id = ubpt.user_id
                     left join payments r on r.request_id = pr.id
                     join (select wi.payment_id, json_agg(coalesce(gpr.html_url, gcr.html_url, gi.html_url)) urls
                           from work_items wi
                                    left join indexer_exp.github_pull_requests gpr on cast(gpr.id as text) = wi.id
                                    left join indexer_exp.github_code_reviews gcr on gcr.id = wi.id
                                    left join indexer_exp.github_issues gi on cast(gi.id as text) = wi.id
                           group by wi.payment_id) g_urls on g_urls.payment_id = pr.id
            where (coalesce(:invoiceStatuses) is null or i.status in (:invoiceStatuses))
            and (coalesce(:invoiceIds) is null or i.id in (:invoiceIds))
            """, nativeQuery = true)
    List<InvoiceRewardViewEntity> findAllByInvoiceStatusesAndInvoiceIds(final List<String> invoiceStatuses, final List<UUID> invoiceIds);
}
