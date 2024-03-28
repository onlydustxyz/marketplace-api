package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayoutPreferenceViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PayoutPreferenceViewRepository extends JpaRepository<PayoutPreferenceViewEntity, PayoutPreferenceViewEntity.PrimaryKey> {

    @Query(value = """
            select u.id as user_id,
                   p.project_id,
                   pd.name as project_name,
                   pd.logo_url as project_logo_url,
                   pd.short_description as project_short_description,
                   pd.key as project_key,
                   pp.billing_profile_id,
                   bp.name as billing_profile_name,
                   bp.type as billing_profile_type
            from iam.users u
            join (select distinct r.project_id, r.recipient_id  from rewards r) p on p.recipient_id = u.github_user_id
            join project_details pd on pd.project_id = p.project_id
            left join accounting.payout_preferences pp on pp.project_id = p.project_id and pp.user_id = u.id
            left join accounting.billing_profiles bp on bp.id = pp.billing_profile_id
            where u.id = :userId
            order by pd.name
            """, nativeQuery = true)
    List<PayoutPreferenceViewEntity> findAllForUser(UUID userId);
}
