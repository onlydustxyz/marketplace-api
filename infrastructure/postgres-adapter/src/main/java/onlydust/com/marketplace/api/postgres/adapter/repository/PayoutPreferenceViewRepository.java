package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayoutPreferenceViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PayoutPreferenceViewRepository extends JpaRepository<PayoutPreferenceViewEntity, PayoutPreferenceViewEntity.PrimaryKey> {

    @Query(value = """
            select u.id as user_id,
                   p.id as project_id,
                   p.name as project_name,
                   p.logo_url as project_logo_url,
                   p.short_description as project_short_description,
                   p.slug as project_key,
                   pp.billing_profile_id
            from iam.users u
            join (select distinct r.project_id, r.recipient_id  from rewards r) r on r.recipient_id = u.github_user_id
            join projects p on p.id = r.project_id
            left join accounting.payout_preferences pp on pp.project_id = r.project_id and pp.user_id = u.id
            where u.id = :userId
            order by p.name
            """, nativeQuery = true)
    List<PayoutPreferenceViewEntity> findAllForUser(UUID userId);
}
