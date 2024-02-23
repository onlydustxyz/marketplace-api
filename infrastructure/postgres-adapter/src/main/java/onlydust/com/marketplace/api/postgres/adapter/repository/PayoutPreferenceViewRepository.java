package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayoutPreferenceViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PayoutPreferenceViewRepository extends JpaRepository<PayoutPreferenceViewEntity, PayoutPreferenceViewEntity.PrimaryKey> {

    @Query(value = """
            select u.id as user_id,
                   r.project_id,
                   pd.name as project_name,
                   pd.logo_url as project_logo_url,
                   pd.short_description as project_short_description,
                   pd.key as project_key,
                   pp.billing_profile_id,
                   bf.name as billing_profile_name,
                   bf.type as billing_profile_type
            from rewards r
            join iam.users u on u.github_user_id = r.recipient_id and u.id = :userId
            join project_details pd on pd.project_id = r.project_id
            left join accounting.payout_preferences pp on pp.project_id = r.project_id and pp.user_id = :userId
            left join accounting.billing_profiles bf on bf.id = pp.billing_profile_id
            order by pd.name
            """, nativeQuery = true)
    List<PayoutPreferenceViewEntity> findAllForUser(UUID userId);
}
