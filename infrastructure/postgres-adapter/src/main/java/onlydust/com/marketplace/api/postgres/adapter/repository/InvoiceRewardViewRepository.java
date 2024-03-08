package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.InvoiceRewardViewEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InvoiceRewardViewRepository extends JpaRepository<InvoiceRewardViewEntity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
            select r.id                                   reward_id,
                   r.amount,
                   r.requested_at,
                   pd.name                                project_name,
                   pd.logo_url                            project_logo_url,
                   s2.s_list                              sponsors,
                   g_urls.urls                            github_urls,
                   rsd.amount_usd_equivalent              dollars_equivalent,
                   i.billing_profile_id                   billing_profile_id,
                   i.data->'billingProfileSnapshot'->>'type' billing_profile_type,
                   case
                       when kyc is not null then kyc.first_name || ' ' || kyc.last_name
                       when kyb is not null then kyb.name
                   end                                    billing_profile_name,
                       
                   creator.info                           invoice_creator,
                   
                   c.name                                 currency_name,
                   c.code                                 currency_code,
                   c.logo_url                             currency_logo_url,
                   receipts.processed_at,
                   receipts.transaction_references
            from rewards r
                     join accounting.reward_status_data rsd on rsd.reward_id = r.id
                     join currencies c on c.id = r.currency_id
                     join project_details pd on r.project_id = pd.project_id
                     left join accounting.invoices i on i.id = r.invoice_id
                     left join accounting.kyb kyb on kyb.billing_profile_id = i.billing_profile_id
                     left join accounting.kyc kyc on kyc.billing_profile_id = i.billing_profile_id
                     
                     left join (select creator.id, jsonb_build_object(
                                        'login', coalesce(creator_ga.login, creator.github_login),
                                        'avatarUrl', user_avatar_url(creator.github_user_id, creator_ga.avatar_url),
                                        'email',creator.email,
                                        'firstName',creator_kyc.first_name,
                                        'lastName',creator_kyc.last_name
                                ) as info
                                from iam.users creator
                                join accounting.kyc creator_kyc on creator_kyc.owner_id = creator.id
                                left join indexer_exp.github_accounts creator_ga on creator_ga.id = creator.github_user_id
                                ) creator on creator.id = i.created_by
                     
                     left join (select ps2.project_id, jsonb_agg(json_build_object('name', s.name, 'logoUrl', s.logo_url)) s_list
                                from sponsors s
                                         join projects_sponsors ps2 on ps2.sponsor_id = s.id
                                group by ps2.project_id) s2 on s2.project_id = r.project_id
                     join iam.users u on u.github_user_id = r.recipient_id
                     left join user_profile_info upi on upi.id = u.id
                     left join (select rr.reward_id,
                                       jsonb_agg(re.transaction_reference) transaction_references,
                                       max(re.created_at) processed_at
                                from accounting.receipts re
                                join accounting.rewards_receipts rr on rr.receipt_id = re.id
                                group by rr.reward_id) receipts on receipts.reward_id = r.id
                     left join (select wi.payment_id, jsonb_agg(coalesce(gpr.html_url, gcr.html_url, gi.html_url)) urls
                           from work_items wi
                                    left join indexer_exp.github_pull_requests gpr on cast(gpr.id as text) = wi.id
                                    left join indexer_exp.github_code_reviews gcr on gcr.id = wi.id
                                    left join indexer_exp.github_issues gi on cast(gi.id as text) = wi.id
                           group by wi.payment_id) g_urls on g_urls.payment_id = r.id
                           
            """;

    @Query(value = SELECT + """
            where (coalesce(:invoiceStatuses) is null or cast(i.status as text) in (:invoiceStatuses))
            and (coalesce(:invoiceIds) is null or i.id in (:invoiceIds))
            """, nativeQuery = true)
    List<InvoiceRewardViewEntity> findAllByInvoiceStatusesAndInvoiceIds(List<String> invoiceStatuses, List<UUID> invoiceIds);

    @Query(value = SELECT + """
            where i.id = :invoiceId
            """, nativeQuery = true)
    List<InvoiceRewardViewEntity> findAllByInvoiceId(@NonNull UUID invoiceId);


    @Query(value = SELECT + """
            where r.id in (:rewardIds)
            """, nativeQuery = true)
    List<InvoiceRewardViewEntity> findAllByRewardIds(@NonNull List<UUID> rewardIds);

}
