package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.UserProfileProjectEarningsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface UserProfileProjectEarningsEntityRepository extends Repository<UserProfileProjectEarningsEntity, String> {
    @Query(value = """
            select r.project_id       as project_id,
                   sum(r.usd_amount)  as total_earned_usd
            from bi.p_reward_data r
                join bi.p_project_global_data p on r.project_id = p.project_id
            where r.contributor_id = :githubUserId and
                  (:ecosystemId is null or :ecosystemId = any (p.ecosystem_ids)) and
                  (cast(:fromDate as text) is null or r.timestamp >= :fromDate) and
                  (cast(:toDate as text) is null or r.timestamp < cast(:toDate as timestamptz) + interval '1 day')
            group by r.project_id
            """, nativeQuery = true)
    List<UserProfileProjectEarningsEntity> findByContributorIdAndEcosystem(Long githubUserId, UUID ecosystemId, ZonedDateTime fromDate, ZonedDateTime toDate);
}
