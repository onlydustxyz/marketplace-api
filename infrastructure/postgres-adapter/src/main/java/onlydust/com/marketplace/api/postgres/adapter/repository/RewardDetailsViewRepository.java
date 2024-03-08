package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.RewardDetailsViewEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface RewardDetailsViewRepository extends JpaRepository<RewardDetailsViewEntity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
                select r.id                                     id,
                               rs.status_for_user                       status,
                               r.requested_at                           requested_at,
                               receipts.processed_at                    processed_at,

                               github_recipient.id                      recipient_id,
                               github_recipient.login                   recipient_login,
                               user_avatar_url(r.recipient_id, github_recipient.avatar_url) recipient_avatar_url,

                               g_urls.urls                              github_urls,

                               pd.project_id                            project_id,
                               pd.name                                  project_name,
                               pd.logo_url                              project_logo_url,
                               pd.short_description                     project_short_description,
                               pd.key                                   project_slug,

                               s2.s_list                                sponsors,

                               rsd.amount_usd_equivalent                dollars_equivalent,
                               r.amount                                 amount,
                               c.name                                   currency_name,
                               c.code                                   currency_code,
                               c.logo_url                               currency_logo_url,

                               i.billing_profile_id                     billing_profile_id,
                               bp.type                                  billing_profile_type,
                               case
                                   when kyc is not null then kyc.first_name || ' ' || kyc.last_name
                                   when kyb is not null then kyb.name
                               end                                      billing_profile_name,
                               bp.verification_status                   billing_profile_verification_status,

                               creator.info                             invoice_creator,

                               i.id                                     invoice_id,
                               i.number                                 invoice_number,
                               i.status                                 invoice_status,

                               receipts.transaction_references,
                               receipts.paid_to_account_numbers

                        from rewards r
                                 join accounting.reward_statuses rs on rs.reward_id = r.id
                                 join accounting.reward_status_data rsd on rsd.reward_id = r.id
                                 join currencies c on c.id = r.currency_id
                                 join project_details pd on r.project_id = pd.project_id
                                 left join indexer_exp.github_accounts github_recipient ON github_recipient.id = r.recipient_id

                                 left join accounting.invoices i on i.id = r.invoice_id
                                 left join accounting.billing_profiles bp on bp.id = i.billing_profile_id
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
                                                   jsonb_agg(re.third_party_account_number) paid_to_account_numbers,
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
                        
                     
                       
            where cast(rs.status_for_user as text) in (:statuses)
                and (coalesce(:fromRequestedAt) is null or r.requested_at >= cast(cast(:fromRequestedAt as text) as timestamp))
                and (coalesce(:toRequestedAt)   is null or r.requested_at <= cast(cast(:toRequestedAt   as text) as timestamp))
                and (coalesce(:fromProcessedAt) is null or receipts.processed_at  >= cast(cast(:fromProcessedAt as text) as timestamp))
                and (coalesce(:toProcessedAt)   is null or receipts.processed_at  <= cast(cast(:toProcessedAt   as text) as timestamp))
            """, nativeQuery = true)
    Page<RewardDetailsViewEntity> findAllByStatusesAndDates(@NonNull List<String> statuses,
                                                            Date fromRequestedAt, Date toRequestedAt,
                                                            Date fromProcessedAt, Date toProcessedAt,
                                                            Pageable pageable);

    @Query(value = SELECT + """
            where (coalesce(:invoiceStatuses) is null or cast(i.status as text) in (:invoiceStatuses))
            and (coalesce(:invoiceIds) is null or i.id in (:invoiceIds))
            """, nativeQuery = true)
    List<RewardDetailsViewEntity> findAllByInvoiceStatusesAndInvoiceIds(List<String> invoiceStatuses, List<UUID> invoiceIds);

    @Query(value = SELECT + """
            where i.id = :invoiceId
            """, nativeQuery = true)
    List<RewardDetailsViewEntity> findAllByInvoiceId(@NonNull UUID invoiceId);


    @Query(value = SELECT + """
            where r.id in (:rewardIds)
            """, nativeQuery = true)
    List<RewardDetailsViewEntity> findAllByRewardIds(@NonNull List<UUID> rewardIds);

    @Query(nativeQuery = true, value = SELECT + " where receipts.processed_at is not null and r.payment_notified_at is null")
    List<RewardDetailsViewEntity> findPaidRewardsToNotify();
}
