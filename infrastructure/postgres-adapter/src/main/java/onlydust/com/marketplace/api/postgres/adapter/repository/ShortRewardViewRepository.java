package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
                                             join accounting.reward_statuses rs on rs.reward_id = r.id
                                             join accounting.reward_status_data rsd on rsd.reward_id = r.id
                                             join currencies c on c.id = r.currency_id
                                             join projects p on r.project_id = p.id
                                             left join iam.users u on u.github_user_id = r.recipient_id
                                             left join user_profile_info upi on upi.id = u.id
            where r.id = :rewardId
            """)
    Optional<ShortRewardViewEntity> findById(UUID rewardId);
}
