package onlydust.com.marketplace.api.read.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.TimeGroupingEnum;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedContributorKpisReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedMergedPrKpisReadEntity;
import org.intellij.lang.annotations.Language;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class AggregatedContributorKpisReadRepository {
    private final EntityManager entityManager;

    @Language("PostgreSQL")
    private final static String SELECT_QUERY = """
            WITH aggregated_contributor_stats AS
                     (SELECT d.timestamp                                                                 as timestamp,
                             count(d.contributor_id)                                                     as active_contributor_count,
            
                             count(d.contributor_id)
                             filter (where previous.timestamp is null)                                   as new_contributor_count,
            
                             count(d.contributor_id)
                             filter (where previous.timestamp < d.timestamp -
                                                                cast(:timeGroupingInterval as interval)) as reactivated_contributor_count
                      from bi.%s_contributor_data d
                               left join lateral ( select max(previous.timestamp) as timestamp
                                                   from bi.%s_contributor_data previous
                                                   where previous.contributor_id = d.contributor_id
                                                     and previous.timestamp < d.timestamp
                                                     and (coalesce(:programOrEcosystemIds) is null
                                                       or cast(:programOrEcosystemIds as uuid[]) && previous.program_ids
                                                       or cast(:programOrEcosystemIds as uuid[]) && previous.ecosystem_ids)) previous on true
                      where
                        -- We need to get one interval before fromDate to calculate churned contributor count
                          d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(:timeGroupingInterval as interval)
                        and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:programOrEcosystemIds) is null
                          or cast(:programOrEcosystemIds as uuid[]) && d.program_ids
                          or cast(:programOrEcosystemIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_rewards_stats AS
                     (SELECT date_trunc(:timeGrouping, d.timestamp) as timestamp,
                             sum(d.usd_amount)                      as total_rewarded_usd_amount
                      from bi.reward_data d
                      where d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:programOrEcosystemIds) is null
                          or cast(:programOrEcosystemIds as uuid[]) && d.program_ids
                          or cast(:programOrEcosystemIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_grants_stats AS
                     (SELECT date_trunc(:timeGrouping, d.day_timestamp) as timestamp,
                             sum(d.usd_amount)                          as total_granted_usd_amount
                      from bi.daily_project_grants d
                      where d.day_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.day_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:programOrEcosystemIds) is null or d.program_id = any (cast(:programOrEcosystemIds as uuid[])))
                      group by 1),
            
                 all_timestamps_to_return AS
                     (SELECT series.timestamp                                             as timestamp,
                             (series.timestamp - cast(:timeGroupingInterval as interval)) as timestamp_of_previous_period
                      -- We need to get one interval before fromDate to calculate churned project count
                      FROM (select generate_series(date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(:timeGroupingInterval as interval),
                                                   date_trunc(:timeGrouping, cast(:toDate as timestamptz)),
                                                   cast(:timeGroupingInterval as interval)) as timestamp) series)
            
            SELECT allt.timestamp,
                   allt.timestamp_of_previous_period,
                   acs.active_contributor_count,
                   acs.new_contributor_count,
                   acs.reactivated_contributor_count,
                   apgs.total_granted_usd_amount,
                   aprs.total_rewarded_usd_amount
            FROM all_timestamps_to_return allt
                     LEFT JOIN aggregated_contributor_stats acs
                               ON allt.timestamp = acs.timestamp
                     LEFT JOIN aggregated_project_grants_stats apgs
                               ON allt.timestamp = apgs.timestamp
                     LEFT JOIN aggregated_project_rewards_stats aprs
                               ON allt.timestamp = aprs.timestamp
            """;

    public List<AggregatedContributorKpisReadEntity> findAll(@NonNull TimeGroupingEnum timeGrouping,
                                                             @NonNull String timeGroupingInterval,
                                                             @NonNull ZonedDateTime fromDate,
                                                             @NonNull ZonedDateTime toDate,
                                                             UUID[] programOrEcosystemIds) {
        final var query = entityManager.createNativeQuery(String.format(SELECT_QUERY, timeGrouping.name().toLowerCase(), timeGrouping.name().toLowerCase()),
                AggregatedContributorKpisReadEntity.class);
        query.setParameter("timeGrouping", timeGrouping.name());
        query.setParameter("timeGroupingInterval", timeGroupingInterval);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("programOrEcosystemIds", programOrEcosystemIds);
        return query.getResultList();
    }

    public List<AggregatedMergedPrKpisReadEntity> findMergedPrs(@NonNull TimeGroupingEnum timeGrouping,
                                                                @NonNull String timeGroupingInterval,
                                                                @NonNull ZonedDateTime fromDate,
                                                                @NonNull ZonedDateTime toDate,
                                                                UUID[] programOrEcosystemIds) {
        final var query = entityManager.createNativeQuery("""
                        SELECT date_trunc(:timeGrouping, d.timestamp) as timestamp,
                               count(d.contribution_id)               as merged_pr_count
                        from bi.contribution_data d
                        where d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                          and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                          and (coalesce(:programOrEcosystemIds) is null
                            or cast(:programOrEcosystemIds as uuid[]) && d.program_ids
                            or cast(:programOrEcosystemIds as uuid[]) && d.ecosystem_ids)
                          and d.is_merged_pr
                        group by 1;
                        """,
                AggregatedMergedPrKpisReadEntity.class);
        query.setParameter("timeGrouping", timeGrouping.name());
        query.setParameter("timeGroupingInterval", timeGroupingInterval);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("programOrEcosystemIds", programOrEcosystemIds);
        return query.getResultList();
    }
}
