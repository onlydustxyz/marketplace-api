package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.InvoiceRewardViewEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InvoiceRewardViewRepository extends JpaRepository<InvoiceRewardViewEntity, UUID> {

    @Language("PostgreSQL")
    String SELECT = """
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
                   ubpt.billing_profile_type,
                   cbp.name company_name,
                   cbp.id company_id,
                   cbp.verification_status company_verification_status,
                   cbp.user_id company_owner_id,
                   cbp.registration_number company_number,
                   cbp.registration_date company_registration_date,
                   cbp.address company_address,
                   cbp.country company_country,
                   cbp.us_entity company_us_entity,
                   cbp.subject_to_eu_vat company_subject_to_eu_vat,
                   cbp.eu_vat_number company_eu_vat_number,
                   cbp.updated_at company_updated_at,
                   cbp.applicant_id company_applicant_id,
                   ibp.id individual_id,
                   ibp.user_id individual_owner_id,
                   ibp.verification_status individual_verification_status,
                   ibp.first_name individual_first_name,
                   ibp.last_name individual_last_name,
                   ibp.address individual_address,
                   ibp.country individual_country,
                   ibp.birthdate individual_birthdate,
                   ibp.valid_until individual_valid_until,
                   ibp.id_document_number individual_id_document_number,
                   ibp.id_document_type individual_id_document_type,
                   ibp.id_document_country_code individual_id_document_country_code,
                   ibp.us_citizen individual_us_citizen,
                   ibp.updated_at individual_updated_at,
                   ibp.applicant_id individual_applicant_id,
                   c.name                                 currency_name,
                   c.code                                 currency_code,
                   c.logo_url                             currency_logo_url,
                   case
                        when r.receipt -> 'Ethereum' is not null then r.receipt -> 'Ethereum' ->> 'transaction_hash'
                        when r.receipt -> 'Optimism' is not null then r.receipt -> 'Optimism' ->> 'transaction_hash'
                        when r.receipt -> 'Aptos' is not null then r.receipt -> 'Aptos' ->> 'transaction_hash'
                        when r.receipt -> 'Starknet' is not null then r.receipt -> 'Starknet' ->> 'transaction_hash'
                        when r.receipt -> 'Sepa' is not null then r.receipt -> 'Sepa' ->> 'transaction_reference'
                        end                                transaction_hash
            from accounting.invoices i
                     join payment_requests pr on pr.invoice_id = i.id
                     join currencies c on c.code = upper(cast(pr.currency as text))
                     join project_details pd on pr.project_id = pd.project_id
                     left join (select ps2.project_id, json_agg(json_build_object('name', s.name, 'logoUrl', s.logo_url)) s_list
                                from sponsors s
                                         join projects_sponsors ps2 on ps2.sponsor_id = s.id
                                group by ps2.project_id) s2 on s2.project_id = pr.project_id
                     join iam.users u on u.github_user_id = pr.recipient_id
                     left join user_profile_info upi on upi.id = u.id
                     join user_billing_profile_types ubpt on ubpt.user_id = u.id
                     left join individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                     left join company_billing_profiles cbp on cbp.user_id = ubpt.user_id
                     left join payments r on r.request_id = pr.id
                     left join (select wi.payment_id, json_agg(coalesce(gpr.html_url, gcr.html_url, gi.html_url)) urls
                           from work_items wi
                                    left join indexer_exp.github_pull_requests gpr on cast(gpr.id as text) = wi.id
                                    left join indexer_exp.github_code_reviews gcr on gcr.id = wi.id
                                    left join indexer_exp.github_issues gi on cast(gi.id as text) = wi.id
                           group by wi.payment_id) g_urls on g_urls.payment_id = pr.id
                           
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
                   ubpt.billing_profile_type,
                   cbp.name company_name,
                   cbp.id company_id,
                   cbp.verification_status company_verification_status,
                   cbp.user_id company_owner_id,
                   cbp.registration_number company_number,
                   cbp.registration_date company_registration_date,
                   cbp.address company_address,
                   cbp.country company_country,
                   cbp.us_entity company_us_entity,
                   cbp.subject_to_eu_vat company_subject_to_eu_vat,
                   cbp.eu_vat_number company_eu_vat_number,
                   cbp.updated_at company_updated_at,
                   cbp.applicant_id company_applicant_id,
                   ibp.id individual_id,
                   ibp.user_id individual_owner_id,
                   ibp.verification_status individual_verification_status,
                   ibp.first_name individual_first_name,
                   ibp.last_name individual_last_name,
                   ibp.address individual_address,
                   ibp.country individual_country,
                   ibp.birthdate individual_birthdate,
                   ibp.valid_until individual_valid_until,
                   ibp.id_document_number individual_id_document_number,
                   ibp.id_document_type individual_id_document_type,
                   ibp.id_document_country_code individual_id_document_country_code,
                   ibp.us_citizen individual_us_citizen,
                   ibp.updated_at individual_updated_at,
                   ibp.applicant_id individual_applicant_id,
                   c.name                                 currency_name,
                   c.code                                 currency_code,
                   c.logo_url                             currency_logo_url,
                   case
                        when r.receipt -> 'Ethereum' is not null then r.receipt -> 'Ethereum' ->> 'transaction_hash'
                        when r.receipt -> 'Optimism' is not null then r.receipt -> 'Optimism' ->> 'transaction_hash'
                        when r.receipt -> 'Aptos' is not null then r.receipt -> 'Aptos' ->> 'transaction_hash'
                        when r.receipt -> 'Starknet' is not null then r.receipt -> 'Starknet' ->> 'transaction_hash'
                        when r.receipt -> 'Sepa' is not null then r.receipt -> 'Sepa' ->> 'transaction_reference'
                        end                                transaction_hash
            from payment_requests pr
                     join currencies c on c.code = upper(cast(pr.currency as text))
                     join project_details pd on pr.project_id = pd.project_id
                     left join (select ps2.project_id, json_agg(json_build_object('name', s.name, 'logoUrl', s.logo_url)) s_list
                                from sponsors s
                                         join projects_sponsors ps2 on ps2.sponsor_id = s.id
                                group by ps2.project_id) s2 on s2.project_id = pr.project_id
                     join iam.users u on u.github_user_id = pr.recipient_id
                     left join user_profile_info upi on upi.id = u.id
                     join user_billing_profile_types ubpt on ubpt.user_id = u.id
                     left join individual_billing_profiles ibp on ibp.user_id = ubpt.user_id
                     left join company_billing_profiles cbp on cbp.user_id = ubpt.user_id
                     left join payments r on r.request_id = pr.id
                     left join (select wi.payment_id, json_agg(coalesce(gpr.html_url, gcr.html_url, gi.html_url)) urls
                           from work_items wi
                                    left join indexer_exp.github_pull_requests gpr on cast(gpr.id as text) = wi.id
                                    left join indexer_exp.github_code_reviews gcr on gcr.id = wi.id
                                    left join indexer_exp.github_issues gi on cast(gi.id as text) = wi.id
                           group by wi.payment_id) g_urls on g_urls.payment_id = pr.id
                           where pr.id in (:rewardIds)""", nativeQuery = true)
    List<InvoiceRewardViewEntity> findAllByRewardIds(@NonNull List<UUID> rewardIds);

    @Query(nativeQuery = true, value = SELECT + " where r.processed_at is not null and pr.payment_notified_at is null")
    List<InvoiceRewardViewEntity> findPaidRewardsToNotify();


}
