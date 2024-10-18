package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.reward.RewardV2ReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RewardReadV2Repository extends JpaRepository<RewardV2ReadEntity, UUID> {

    @Query(value = """
            select r.reward_id             as id,
                   r.status                as status,
                   r.project_id            as project_id,
                   r.billing_profile_id    as billing_profile_id,
                   r.currency_id           as currency_id,
                   r.amount                as amount,
                   jsonb_build_object(
                           'id', requestor.user_id,
                           'githubUserId', requestor.github_user_id,
                           'login', requestor.login,
                           'avatarUrl', requestor.avatar_url,
                           'isRegistered', requestor.user_id is not null
                   )                       as requestor,
                   r.requestor_id          as requestor_id,
                   jsonb_build_object(
                           'id', recipient.user_id,
                           'githubUserId', recipient.github_user_id,
                           'login', recipient.login,
                           'avatarUrl', recipient.avatar_url,
                           'isRegistered', recipient.user_id is not null
                   )                       as recipient,
                   r.recipient_id          as recipient_id,
                   r.requested_at          as requested_at,
                   r.paid_at               as processed_at,
                   r.unlock_date           as unlock_date,
                   r.usd_conversion_rate   as usd_conversion_rate,
                   r.amount_usd_equivalent as amount_usd_equivalent
            from accounting.reward_statuses r
                     join iam.all_indexed_users requestor on r.requestor_id = requestor.user_id
                     join iam.all_indexed_users recipient on r.recipient_id = recipient.github_user_id
            
            where (cast(:dataSourceIds as uuid[]) is not null and r.project_id = any (cast(:dataSourceIds as uuid[])) or r.recipient_id = :dataSourceRecipientId)
              and (cast(:statuses as accounting.reward_status[]) is null or r.status = any (cast(:statuses as accounting.reward_status[])))
              and (cast(:projectIds as uuid[]) is null or r.project_id = any (cast(:projectIds as uuid[])))
              and (cast(:recipientIds as bigint[]) is null or r.recipient_id = any (cast(:recipientIds as bigint[])))
              and (cast(:contributionUUIDs as uuid[]) is null or r.reward_id = any (get_reward_ids_of_contributions(cast(:contributionUUIDs as uuid[]))))
            """, nativeQuery = true)
    Page<RewardV2ReadEntity> findAll(UUID[] dataSourceIds,
                                     Long dataSourceRecipientId,
                                     String[] statuses,
                                     UUID[] projectIds,
                                     UUID[] contributionUUIDs,
                                     Long[] recipientIds,
                                     Pageable pageable);
}
