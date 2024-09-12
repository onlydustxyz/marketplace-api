package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedContributorKpisReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface AggregatedContributorKpisReadRepository extends Repository<AggregatedContributorKpisReadEntity, ZonedDateTime> {

    @Query(value = """
            WITH aggregated_project_stats AS
                     (SELECT date_trunc(:timeGrouping, d.timestamp)                                                       as timestamp,
                             date_trunc(:timeGrouping, d.timestamp) - cast(('1 ' || :timeGrouping) as interval)           as timestamp_of_previous_period,
                             count(distinct d.contributor_id)                                                             as active_contributor_count,
            
                             count(distinct d.contributor_id)
                             filter (where d.previous_contributor_contribution_timestamp is null)                         as new_contributor_count,
            
                             count(distinct d.contributor_id)
                             filter (where d.previous_contributor_contribution_timestamp < date_trunc(:timeGrouping, d.timestamp) -
                                                                               cast(('1 ' || :timeGrouping) as interval)) as reactivated_contributor_count,
            
                             count(distinct d.contributor_id)
                             filter (where d.next_contributor_contribution_timestamp is null and date_trunc(:timeGrouping, d.timestamp) <
                                                                                     date_trunc(:timeGrouping, now()))    as next_period_churned_contributor_count,
            
                             count(distinct d.contribution_id)
                             filter ( where d.is_merged_pr )                                                              as merged_pr_count
                      from bi.contribution_data d
                      where
                        -- We need to get one interval before fromDate to calculate churned project count
                          d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(('1 ' || :timeGrouping) as interval)
                        and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(('1 ' || :timeGrouping) as interval)
                        and (coalesce(:programOrEcosystemIds) is null
                          or cast(:programOrEcosystemIds as uuid[]) && d.program_ids
                          or cast(:programOrEcosystemIds as uuid[]) && d.ecosystem_ids)
                      group by 1, 2),
            
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
                     -- We need to get one interval before fromDate to calculate churned project count
                     (SELECT generate_series(date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(('1 ' || :timeGrouping) as interval),
                                             date_trunc(:timeGrouping, cast(:toDate as timestamptz)),
                                             cast(('1 ' || :timeGrouping) as interval)) AS timestamp)
            
            SELECT allt.timestamp,
                   aps.timestamp_of_previous_period,
                   aps.active_contributor_count,
                   aps.new_contributor_count,
                   aps.reactivated_contributor_count,
                   aps.next_period_churned_contributor_count,
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
    List<AggregatedContributorKpisReadEntity> findAll(@NonNull String timeGrouping,
                                                      @NonNull ZonedDateTime fromDate,
                                                      @NonNull ZonedDateTime toDate,
                                                      List<UUID> programOrEcosystemIds);
}
