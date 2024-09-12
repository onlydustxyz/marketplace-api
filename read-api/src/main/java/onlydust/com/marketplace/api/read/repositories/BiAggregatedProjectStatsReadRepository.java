package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.bi.BiAggregatedProjectStatsReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface BiAggregatedProjectStatsReadRepository extends Repository<BiAggregatedProjectStatsReadEntity, ZonedDateTime> {

    @Query(value = """
            SELECT date_trunc(:timeGrouping, d.timestamp)                                             as timestamp,
                   date_trunc(:timeGrouping, d.timestamp) - cast(('1 ' || :timeGrouping) as interval) as timestamp_of_previous_period,
                   count(distinct d.project_id)                                                       as active_project_count,
            
                   count(distinct d.project_id)
                   filter (where d.previous_contribution_timestamp is null)                           as new_project_count,
            
                   count(distinct d.project_id)
                   filter (where d.previous_contribution_timestamp < date_trunc(:timeGrouping, d.timestamp) -
                                                                     cast(('1 ' || :timeGrouping) as interval))  as reactivated_project_count,
            
                   count(distinct d.project_id)
                   filter (where d.next_contribution_timestamp is null and date_trunc(:timeGrouping, d.timestamp) <
                                                                           date_trunc(:timeGrouping, now())) as next_period_churned_project_count,
            
                   dist_sum(distinct d.project_id, d.merged_pr_count)                                 as merged_pr_count
            from bi.project_contribution_data d
            where d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(('1 ' || :timeGrouping) as interval)
              and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(('1 ' || :timeGrouping) as interval)
              and (coalesce(:programOrEcosystemIds) is null
                       or cast(:programOrEcosystemIds as uuid[]) && d.program_ids
                       or cast(:programOrEcosystemIds as uuid[]) && d.ecosystem_ids)
            group by 1, 2;
            """, nativeQuery = true)
    List<BiAggregatedProjectStatsReadEntity> findAll(@NonNull String timeGrouping,
                                                     @NonNull ZonedDateTime fromDate,
                                                     @NonNull ZonedDateTime toDate,
                                                     List<UUID> programOrEcosystemIds);
}
