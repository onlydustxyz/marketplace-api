package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortRewardQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortRewardViewRepository extends JpaRepository<ShortRewardQueryEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select r.id                                     id,
                                   r.recipient_id                           recipient_id,
                                   r.requestor_id                           requestor_id,
                                   p.id                                     project_id,
                                   p.name                                   project_name,
                                   p.logo_url                               project_logo_url,
                                   p.short_description                      project_short_description,
                                   p.slug                                    project_slug,
                                   r.amount                                  amount,
                                   c.id                                      currency_id
                                    from rewards r
                                             join currencies c on c.id = r.currency_id
                                             join projects p on r.project_id = p.id
                                             left join iam.users u on u.github_user_id = r.recipient_id
                                             left join user_profile_info upi on upi.id = u.id
            where r.id = :rewardId
            """)
    Optional<ShortRewardQueryEntity> findById(UUID rewardId);


    @Query(nativeQuery = true, value = """
            with ecosystem_rewards as (select r.id                                                     id,
                                r.requestor_id                                           requestor_id,
                                r.recipient_id                                           recipient_id,
                                r.requested_at                                           requested_at,
                                p.id                                                     project_id,
                                p.name                                                   project_name,
                                p.logo_url                                               project_logo_url,
                                p.short_description                                      project_short_description,
                                p.slug                                                    project_slug,
                                r.amount                                                 amount,
                                c.id                                                     currency_id
                         from ecosystems e
                                  join projects_ecosystems pe on pe.ecosystem_id = e.id
                                  join projects p on p.id = pe.project_id
                                  join rewards r on r.project_id = p.id
                                  join currencies c on c.id = r.currency_id
                         where e.id = :ecosystemId and p.id != :projectId),
            first_boost as (select count(*) = 0 as is_true from node_guardians_boost_rewards)
            select ecosystem_rewards.*
            from ecosystem_rewards
            left join node_guardians_boost_rewards ngbr on ngbr.boosted_reward_id = ecosystem_rewards.id
            join first_boost fb on true
            join currencies c on c.id = ecosystem_rewards.currency_id and c.code = 'STRK'
            where (
                (fb.is_true and ecosystem_rewards.requested_at >= to_date('01-04-2024', 'dd-MM-yyyy'))
                       or (ngbr.boosted_reward_id is null and ecosystem_rewards.requested_at >= current_date - 7)
                )
            """)
    List<ShortRewardQueryEntity> findRewardsToBoosWithNodeGuardiansForEcosystemIdNotLinkedToProject(UUID ecosystemId, UUID projectId);

    @Query(nativeQuery = true, value = """
            with sub as (select count(*), boost_reward_id
                         from node_guardians_boost_rewards
                         where recipient_id = :recipientId
                         group by boost_reward_id)
            select count(*)
            from sub;
            """)
    Optional<Integer> countNumberOfBoostByRecipientId(Long recipientId);
}
