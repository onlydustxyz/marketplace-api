package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.bi.WorldMapKpiReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface WorldMapKpiReadRepository extends JpaRepository<WorldMapKpiReadEntity, String> {

    @Query(value = """
            select coalesce(cd.contributor_country, 'NULL') as country_code,
                   count(distinct cd.contributor_id)        as value
            from bi.p_per_contributor_contribution_data cd
            where (cast(:fromDate as text) is null or cd.timestamp >= to_date(cast(:fromDate as text), 'YYYY-MM-DD'))
              and (cast(:toDate as text) is null or date_trunc('day', cd.timestamp) < to_date(cast(:toDate as text), 'YYYY-MM-DD'))
              and (coalesce(:dataSourceIds) is null or
                   cd.project_id = any (:dataSourceIds) or
                   cd.program_ids && cast(array [:dataSourceIds] as uuid[]) or
                   cd.ecosystem_ids && cast(array [:dataSourceIds] as uuid[]))
            group by cd.contributor_country
            order by cd.contributor_country
            """, nativeQuery = true)
    List<WorldMapKpiReadEntity> findActiveContributorCount(ZonedDateTime fromDate, ZonedDateTime toDate, UUID[] dataSourceIds);
}
