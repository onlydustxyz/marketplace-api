package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.bi.WorldMapKpiReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface WorldMapKpiReadRepository extends JpaRepository<WorldMapKpiReadEntity, String> {

    @Query(value = """
            select
                kyc.country as country_code,
                count(distinct cd.contributor_id) as value
            from bi.contributor_data cd
            join iam.users u on u.github_user_id = cd.contributor_id
            join accounting.billing_profiles_users bpu on bpu.user_id = u.id
            join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id and kyc.country is not null
            where (cast(:fromDate as text) is null or cd.timestamp >= to_date(cast(:fromDate as text), 'YYYY-MM-DD'))
                and (cast(:toDate as text) is null or date_trunc('day', cd.timestamp) < to_date(cast(:toDate as text), 'YYYY-MM-DD'))
                and (coalesce(:programOrEcosystemIds) is null or
                     cd.program_ids && cast(array[:programOrEcosystemIds] as uuid[]) or
                     cd.ecosystem_ids && cast(array[:programOrEcosystemIds] as uuid[]))
            group by kyc.country
            order by kyc.country
            """, nativeQuery = true)
    List<WorldMapKpiReadEntity> findActiveContributorCount(ZonedDateTime fromDate, ZonedDateTime toDate, UUID[] programOrEcosystemIds);
}
