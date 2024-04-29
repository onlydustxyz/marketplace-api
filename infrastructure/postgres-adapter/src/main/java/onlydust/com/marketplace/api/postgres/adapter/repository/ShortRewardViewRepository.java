package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortRewardViewRepository extends JpaRepository<ShortRewardViewEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select r.id                                     id,
                                   r.recipient_id                           recipient_id,
                                   r.requestor_id                           requestor_id,
                                   p.id                                     project_id,
                                   p.name                                   project_name,
                                   p.logo_url                               project_logo_url,
                                   p.short_description                      project_short_description,
                                   p.key                                    project_slug,
                                   r.amount                                  amount,
                                   c.id                                      currency_id
                                    from rewards r
                                             join currencies c on c.id = r.currency_id
                                             join projects p on r.project_id = p.id
                                             left join iam.users u on u.github_user_id = r.recipient_id
                                             left join user_profile_info upi on upi.id = u.id
            where r.id = :rewardId
            """)
    Optional<ShortRewardViewEntity> findById(UUID rewardId);


    @Query(nativeQuery = true, value = """
            with sub as (select r.id                                                     id,
                                r.recipient_id                                           recipient_id,
                                r.requestor_id                                           requestor_id,
                                r.requested_at                                           requested_at,
                                p.id                                                     project_id,
                                p.name                                                   project_name,
                                p.logo_url                                               project_logo_url,
                                p.short_description                                      project_short_description,
                                p.key                                                    project_slug,
                                r.amount                                                 amount,
                                c.id                                                     currency_id,
                                (select count(*) from node_guardians_boost_rewards) count
                         from ecosystems e
                                  join projects_ecosystems pe on pe.ecosystem_id = e.id
                                  join projects p on p.id = pe.project_id
                                  join rewards r on r.project_id = p.id
                                  join currencies c on c.id = r.currency_id
                                  left join iam.users u on u.github_user_id = r.recipient_id
                                  left join user_profile_info upi on upi.id = u.id
                         where e.id = :ecosystemId and p.id != :projectId)
            select *
            from sub
            left join node_guardians_boost_rewards ngbr on ngbr.boosted_reward_id = sub.id
            where ((sub.count = 0 && sub.requested_at >= to_date('01-04-2024', 'dd-MM-yyyy')) || (ngbr.boosted_reward_id is null and sub.requested_at >= current_date - 7))
            """)
    List<ShortRewardViewEntity> findRewardsToBoosWithNodeGuardiansForEcosystemIdNotLinkedToProject(UUID ecosystemId, UUID projectId);

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
