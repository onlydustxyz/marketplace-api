package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedProjectKpisReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface AggregatedProjectKpisReadRepository extends Repository<AggregatedProjectKpisReadEntity, ZonedDateTime> {

    @Query(value = """
            WITH aggregated_project_stats AS
                     (SELECT date_trunc(:timeGrouping, d.timestamp)                                                               as timestamp,
                             count(distinct d.project_id)                                                                         as active_project_count,
            
                             count(distinct d.project_id)
                             filter (where d.previous_project_contribution_timestamp is null)                                     as new_project_count,
            
                             count(distinct d.project_id)
                             filter (where d.previous_project_contribution_timestamp < date_trunc(:timeGrouping, d.timestamp) -
                                                                                       cast(('1 ' || :timeGrouping) as interval)) as reactivated_project_count,
            
                             count(distinct d.project_id)
                             filter (where (d.next_project_contribution_timestamp is null
                                 or d.next_project_contribution_timestamp >= date_trunc(:timeGrouping, d.timestamp) + cast(('2 ' || :timeGrouping) as interval))
                                 and date_trunc(:timeGrouping, d.timestamp) < date_trunc(:timeGrouping, now()))                   as next_period_churned_project_count,
            
                             count(distinct d.contribution_id)
                             filter ( where d.is_merged_pr )                                                                      as merged_pr_count
                      from bi.contribution_data_cross_projects d
                      where
                        -- We need to get one interval before fromDate to calculate churned project count
                          d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(('1 ' || :timeGrouping) as interval)
                        and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(('1 ' || :timeGrouping) as interval)
                        and (coalesce(:programOrEcosystemIds) is null
                          or cast(:programOrEcosystemIds as uuid[]) && d.program_ids
                          or cast(:programOrEcosystemIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_rewards_stats AS
                     (SELECT date_trunc(:timeGrouping, d.timestamp) as timestamp,
                             sum(d.usd_amount)                      as total_rewarded_usd_amount
                      from bi.reward_data d
                      where d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(('1 ' || :timeGrouping) as interval)
                        and (coalesce(:programOrEcosystemIds) is null
                          or cast(:programOrEcosystemIds as uuid[]) && d.program_ids
                          or cast(:programOrEcosystemIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_grants_stats AS
                     (SELECT date_trunc(:timeGrouping, d.day_timestamp) as timestamp,
                             sum(d.usd_amount)                          as total_granted_usd_amount
                      from bi.daily_project_grants d
                      where d.day_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.day_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(('1 ' || :timeGrouping) as interval)
                        and (coalesce(:programOrEcosystemIds) is null or d.program_id = any (cast(:programOrEcosystemIds as uuid[])))
                      group by 1),
            
                 all_timestamps_to_return AS
                     (SELECT series.timestamp                                               as timestamp,
                             (series.timestamp - cast(('1 ' || :timeGrouping) as interval)) as timestamp_of_previous_period
                      -- We need to get one interval before fromDate to calculate churned project count
                      FROM (select generate_series(date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(('1 ' || :timeGrouping) as interval),
                                                   date_trunc(:timeGrouping, cast(:toDate as timestamptz)),
                                                   cast(('1 ' || :timeGrouping) as interval)) as timestamp) series)
            
            SELECT allt.timestamp,
                   allt.timestamp_of_previous_period,
                   aps.active_project_count,
                   aps.new_project_count,
                   aps.reactivated_project_count,
                   aps.next_period_churned_project_count,
                   aps.merged_pr_count,
                   apgs.total_granted_usd_amount,
                   aprs.total_rewarded_usd_amount
            FROM all_timestamps_to_return allt
                     LEFT JOIN aggregated_project_stats aps
                               ON allt.timestamp = aps.timestamp
                     LEFT JOIN aggregated_project_grants_stats apgs
                               ON allt.timestamp = apgs.timestamp
                     LEFT JOIN aggregated_project_rewards_stats aprs
                               ON allt.timestamp = aprs.timestamp
            """, nativeQuery = true)
    List<AggregatedProjectKpisReadEntity> findAll(@NonNull String timeGrouping,
                                                  @NonNull ZonedDateTime fromDate,
                                                  @NonNull ZonedDateTime toDate,
                                                  UUID[] programOrEcosystemIds);
}
