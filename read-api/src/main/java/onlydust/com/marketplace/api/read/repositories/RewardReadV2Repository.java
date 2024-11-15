package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.reward.RewardV2ReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RewardReadV2Repository extends JpaRepository<RewardV2ReadEntity, UUID> {

    @Query(value = """
            select r.reward_id                               as id,
                   r.status                                  as status,
                   r.project_id                              as project_id,
                   r.billing_profile_id                      as billing_profile_id,
                   r.currency_id                             as currency_id,
                   r.amount                                  as amount,
                   jsonb_build_object(
                           'id', requestor.user_id,
                           'githubUserId', requestor.github_user_id,
                           'login', requestor.login,
                           'avatarUrl', requestor.avatar_url,
                           'isRegistered', requestor.user_id is not null
                   )                                         as requestor,
                   r.requestor_id                            as requestor_id,
                   jsonb_build_object(
                           'id', recipient.user_id,
                           'githubUserId', recipient.github_user_id,
                           'login', recipient.login,
                           'avatarUrl', recipient.avatar_url,
                           'isRegistered', recipient.user_id is not null
                   )                                         as recipient,
                   jsonb_build_object('id', p.id,
                                      'slug', p.slug,
                                      'name', p.name,
                                      'logoUrl', p.logo_url) as project,
                   r.recipient_id                            as recipient_id,
                   r.requested_at                            as requested_at,
                   r.invoice_received_at                     as invoiced_at,
                   r.paid_at                                 as processed_at,
                   r.unlock_date                             as unlock_date,
                   r.usd_conversion_rate                     as usd_conversion_rate,
                   r.amount_usd_equivalent                   as amount_usd_equivalent,
                   r.invoice_id                              as invoice_id,
                   rd.contribution_uuids                     as contribution_uuids,
                   rd.receipt                                as receipt
            from accounting.reward_statuses r
                     join iam.all_indexed_users requestor on r.requestor_id = requestor.user_id
                     join iam.all_indexed_users recipient on r.recipient_id = recipient.github_user_id
                     join projects p on p.id = r.project_id
                     left join bi.p_reward_data rd on r.reward_id = rd.reward_id
            
            where (:includeProjectLeds and cast(:dataSourceProjectLedIds as uuid[]) is not null and r.project_id = any (cast(:dataSourceProjectLedIds as uuid[]))
                or :includeBillingProfileAdministrated and cast(:dataSourceBillingProfileIds as uuid[]) is not null and r.billing_profile_id = any (cast(:dataSourceBillingProfileIds as uuid[]))
                or :includeAsRecipient and r.recipient_id = :dataSourceRecipientId)
              and (:rewardId is null or r.reward_id = :rewardId)
              and (cast(:statuses as accounting.reward_status[]) is null or r.status = any (cast(:statuses as accounting.reward_status[])))
              and (cast(:projectIds as uuid[]) is null or r.project_id = any (cast(:projectIds as uuid[])))
              and (cast(:billingProfileIds as uuid[]) is null or r.billing_profile_id = any (cast(:billingProfileIds as uuid[])))
              and (cast(:currencyIds as uuid[]) is null or r.currency_id = any (cast(:currencyIds as uuid[])))
              and (cast(:recipientIds as bigint[]) is null or r.recipient_id = any (cast(:recipientIds as bigint[])))
              and (cast(:contributionUUIDs as uuid[]) is null or r.reward_id = any (get_reward_ids_of_contributions(cast(:contributionUUIDs as uuid[]))))
              and (cast(:search as text) is null or cast(r.status as text) ilike '%' || :search || '%' or rd.search ilike '%' || :search || '%')
            """, nativeQuery = true)
    Page<RewardV2ReadEntity> findAll(boolean includeProjectLeds,
                                     boolean includeBillingProfileAdministrated,
                                     boolean includeAsRecipient,
                                     UUID rewardId,
                                     UUID[] dataSourceProjectLedIds,
                                     UUID[] dataSourceBillingProfileIds,
                                     Long dataSourceRecipientId,
                                     String[] statuses,
                                     UUID[] projectIds,
                                     UUID[] billingProfileIds,
                                     UUID[] currencyIds,
                                     UUID[] contributionUUIDs,
                                     Long[] recipientIds,
                                     String search,
                                     Pageable pageable);
}
