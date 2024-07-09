package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.billing_profile.PayoutPreferenceReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface PayoutPreferenceReadRepository extends Repository<PayoutPreferenceReadEntity, PayoutPreferenceReadEntity.PrimaryKey> {

    @Query(value = """
            select u.id as user_id,
                   r.project_id as project_id,
                   pp.billing_profile_id as billing_profile_id
            from iam.users u
                join (select distinct r.project_id, r.recipient_id  from rewards r) r on r.recipient_id = u.github_user_id
                left join accounting.payout_preferences pp on pp.project_id = r.project_id and pp.user_id = u.id
            where u.id = :userId
            """, nativeQuery = true)
    List<PayoutPreferenceReadEntity> findAllForUser(UUID userId);
}
