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
                cd.contributor_country            as country_code,
                count(distinct cd.contributor_id) as value
            from bi.project_global_data p
                join bi.contribution_data cd on cd.project_id = p.project_id and (cast(:fromDate as text) is null or cd.timestamp >= to_date(cast(:fromDate as text), 'YYYY-MM-DD'))
                and (cast(:toDate as text) is null or date_trunc('day', cd.timestamp) < to_date(cast(:toDate as text), 'YYYY-MM-DD'))
                                       and cd.contributor_country is not null
            where (coalesce(:programOrEcosystemIds) is null or
                     p.program_ids && cast(array[:programOrEcosystemIds] as uuid[]) or
                     p.ecosystem_ids && cast(array[:programOrEcosystemIds] as uuid[]))
            group by cd.contributor_country
            order by cd.contributor_country
            """, nativeQuery = true)
    List<WorldMapKpiReadEntity> findActiveContributorCount(ZonedDateTime fromDate, ZonedDateTime toDate, UUID[] programOrEcosystemIds);
}
