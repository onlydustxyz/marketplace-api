package onlydust.com.marketplace.api.read.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.TimeGroupingEnum;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedKpisReadEntity;
import org.intellij.lang.annotations.Language;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class AggregatedKpisReadRepository {
    private final EntityManager entityManager;

    @Language("PostgreSQL")
    private final static String SELECT_PROJECT_QUERY = """
            WITH aggregated_project_stats AS
                     (SELECT d.#timeGrouping#_timestamp                                                                   as timestamp,
                             count(distinct d.project_id)                                                                 as total_project_count,
            
                             count(distinct d.project_id)
                             filter (where previous.timestamp is null)                                                    as new_project_count,
            
                             count(distinct d.project_id)
                             filter (where previous.timestamp < d.#timeGrouping#_timestamp - cast(:timeGroupingInterval as interval)) as reactivated_project_count,
            
                             coalesce(sum(d.is_pr), 0)                                                                    as merged_pr_count
                      from bi.p_contribution_data d
                               left join lateral ( select max(previous.#timeGrouping#_timestamp) as timestamp
                                                   from bi.p_contribution_data previous
                                                   where previous.project_id = d.project_id
                                                     and previous.#timeGrouping#_timestamp < d.#timeGrouping#_timestamp
                                                     and (coalesce(:dataSourceIds) is null
                                                       or previous.project_id = any (cast(:dataSourceIds as uuid[]))
                                                       or cast(:dataSourceIds as uuid[]) && previous.program_ids
                                                       or cast(:dataSourceIds as uuid[]) && previous.ecosystem_ids)) previous on true
                      where
                        -- We need to get one interval before fromDate to calculate churned project count
                          d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(:timeGroupingInterval as interval)
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:dataSourceIds) is null
                          or d.project_id = any (cast(:dataSourceIds as uuid[]))
                          or cast(:dataSourceIds as uuid[]) && d.program_ids
                          or cast(:dataSourceIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_rewards_stats AS
                     (SELECT d.#timeGrouping#_timestamp     as timestamp,
                             coalesce(sum(d.usd_amount), 0) as total_rewarded_usd_amount
                      from bi.p_reward_data d
                      where d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:dataSourceIds) is null
                          or d.project_id = any (cast(:dataSourceIds as uuid[]))
                          or cast(:dataSourceIds as uuid[]) && d.program_ids
                          or cast(:dataSourceIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_grants_stats AS
                     (SELECT d.#timeGrouping#_timestamp     as timestamp,
                             coalesce(sum(d.usd_amount), 0) as total_granted_usd_amount
                      from bi.p_project_grants_data d
                      where d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:dataSourceIds) is null
                          or d.project_id = any (cast(:dataSourceIds as uuid[]))
                          or d.program_id = any (cast(:dataSourceIds as uuid[]))
                          or cast(:dataSourceIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 all_timestamps_to_return AS
                     (SELECT series.timestamp                                             as timestamp,
                             (series.timestamp - cast(:timeGroupingInterval as interval)) as timestamp_of_previous_period
                      -- We need to get one interval before fromDate to calculate churned project count
                      FROM (select generate_series(date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(:timeGroupingInterval as interval),
                                                   date_trunc(:timeGrouping, cast(:toDate as timestamptz)),
                                                   cast(:timeGroupingInterval as interval)) as timestamp) series)
            
            SELECT allt.timestamp                       as timestamp,
                   allt.timestamp_of_previous_period    as timestamp_of_previous_period,
                   aps.total_project_count              as total_count,
                   aps.new_project_count                as new_count,
                   aps.reactivated_project_count        as reactivated_count,
                   aps.merged_pr_count                  as merged_pr_count,
                   apgs.total_granted_usd_amount        as total_granted_usd_amount,
                   aprs.total_rewarded_usd_amount       as total_rewarded_usd_amount
            FROM all_timestamps_to_return allt
                     LEFT JOIN aggregated_project_stats aps
                               ON allt.timestamp = aps.timestamp
                     LEFT JOIN aggregated_project_grants_stats apgs
                               ON allt.timestamp = apgs.timestamp
                     LEFT JOIN aggregated_project_rewards_stats aprs
                               ON allt.timestamp = aprs.timestamp
            """;

    @Language("PostgreSQL")
    private final static String SELECT_CONTRIBUTOR_QUERY = """
            WITH aggregated_contributor_stats AS
                     (SELECT d.#timeGrouping#_timestamp                                                                   as timestamp,
                             count(distinct d.contributor_id)                                                             as total_contributor_count,
            
                             count(distinct d.contributor_id)
                             filter (where previous.timestamp is null)                                                    as new_contributor_count,
            
                             count(distinct d.contributor_id)
                             filter (where previous.timestamp < d.#timeGrouping#_timestamp - cast(:timeGroupingInterval as interval)) as reactivated_contributor_count,
            
                             coalesce(sum(d.is_pr), 0)                                                                    as merged_pr_count
                      from bi.p_contribution_data d
                               left join lateral ( select max(previous.#timeGrouping#_timestamp) as timestamp
                                                   from bi.p_contribution_data previous
                                                   where previous.contributor_id = d.contributor_id
                                                     and previous.#timeGrouping#_timestamp < d.#timeGrouping#_timestamp
                                                     and (coalesce(:dataSourceIds) is null
                                                       or previous.project_id = any (cast(:dataSourceIds as uuid[]))
                                                       or cast(:dataSourceIds as uuid[]) && previous.program_ids
                                                       or cast(:dataSourceIds as uuid[]) && previous.ecosystem_ids)) previous on true
                      where
                        -- We need to get one interval before fromDate to calculate churned contributor count
                          d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(:timeGroupingInterval as interval)
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:dataSourceIds) is null
                          or d.project_id = any (cast(:dataSourceIds as uuid[]))
                          or cast(:dataSourceIds as uuid[]) && d.program_ids
                          or cast(:dataSourceIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_rewards_stats AS
                     (SELECT d.#timeGrouping#_timestamp     as timestamp,
                             coalesce(sum(d.usd_amount), 0) as total_rewarded_usd_amount
                      from bi.p_reward_data d
                      where d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:dataSourceIds) is null
                          or d.project_id = any (cast(:dataSourceIds as uuid[]))
                          or cast(:dataSourceIds as uuid[]) && d.program_ids
                          or cast(:dataSourceIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_grants_stats AS
                     (SELECT d.#timeGrouping#_timestamp     as timestamp,
                             coalesce(sum(d.usd_amount), 0) as total_granted_usd_amount
                      from bi.p_project_grants_data d
                      where d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:dataSourceIds) is null
                          or d.project_id = any (cast(:dataSourceIds as uuid[]))
                          or d.program_id = any (cast(:dataSourceIds as uuid[]))
                          or cast(:dataSourceIds as uuid[]) && d.ecosystem_ids)
                      group by 1),
            
                 all_timestamps_to_return AS
                     (SELECT series.timestamp                                             as timestamp,
                             (series.timestamp - cast(:timeGroupingInterval as interval)) as timestamp_of_previous_period
                      -- We need to get one interval before fromDate to calculate churned project count
                      FROM (select generate_series(date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(:timeGroupingInterval as interval),
                                                   date_trunc(:timeGrouping, cast(:toDate as timestamptz)),
                                                   cast(:timeGroupingInterval as interval)) as timestamp) series)
            
            SELECT allt.timestamp                       as timestamp,
                   allt.timestamp_of_previous_period    as timestamp_of_previous_period,
                   acs.total_contributor_count          as total_count,
                   acs.new_contributor_count            as new_count,
                   acs.reactivated_contributor_count    as reactivated_count,
                   acs.merged_pr_count                  as merged_pr_count,
                   apgs.total_granted_usd_amount        as total_granted_usd_amount,
                   aprs.total_rewarded_usd_amount       as total_rewarded_usd_amount
            FROM all_timestamps_to_return allt
                     LEFT JOIN aggregated_contributor_stats acs
                               ON allt.timestamp = acs.timestamp
                     LEFT JOIN aggregated_project_grants_stats apgs
                               ON allt.timestamp = apgs.timestamp
                     LEFT JOIN aggregated_project_rewards_stats aprs
                               ON allt.timestamp = aprs.timestamp;
            """;

    @AllArgsConstructor
    private enum Query {
        PROJECT(SELECT_PROJECT_QUERY),
        CONTRIBUTOR(SELECT_CONTRIBUTOR_QUERY);

        private final String query;
    }

    public List<AggregatedKpisReadEntity> findAllProjects(@NonNull TimeGroupingEnum timeGrouping,
                                                          @NonNull String timeGroupingInterval,
                                                          ZonedDateTime fromDate,
                                                          ZonedDateTime toDate,
                                                          UUID[] dataSourceIds) {
        return findAll(Query.PROJECT, timeGrouping, timeGroupingInterval, fromDate, toDate, dataSourceIds);
    }

    public List<AggregatedKpisReadEntity> findAllContributors(@NonNull TimeGroupingEnum timeGrouping,
                                                              @NonNull String timeGroupingInterval,
                                                              ZonedDateTime fromDate,
                                                              ZonedDateTime toDate,
                                                              UUID[] dataSourceIds) {
        return findAll(Query.CONTRIBUTOR, timeGrouping, timeGroupingInterval, fromDate, toDate, dataSourceIds);
    }

    private List findAll(@NonNull Query query,
                         @NonNull TimeGroupingEnum timeGrouping,
                         @NonNull String timeGroupingInterval,
                         ZonedDateTime fromDate,
                         ZonedDateTime toDate,
                         UUID[] dataSourceIds) {
        final var from = fromDate != null ? fromDate : findFirstContributionDate(dataSourceIds).orElse(ZonedDateTime.now());
        final var to = toDate != null ? toDate : ZonedDateTime.now();
        final var timestampColumnPrefix = timeGrouping.name().toLowerCase();

        return entityManager.createNativeQuery(query.query.replaceAll("#timeGrouping#", timestampColumnPrefix), AggregatedKpisReadEntity.class)
                .setParameter("timeGrouping", timeGrouping.name())
                .setParameter("timeGroupingInterval", timeGroupingInterval)
                .setParameter("fromDate", from)
                .setParameter("toDate", to)
                .setParameter("dataSourceIds", dataSourceIds)
                .getResultList();
    }

    private Optional<ZonedDateTime> findFirstContributionDate(UUID[] dataSourceIds) {
        final var query = entityManager.createNativeQuery("""
                SELECT min(timestamp)
                from bi.p_contribution_data
                where (coalesce(:dataSourceIds) is null
                  or project_id = any (cast(:dataSourceIds as uuid[]))
                  or cast(:dataSourceIds as uuid[]) && program_ids
                  or cast(:dataSourceIds as uuid[]) && ecosystem_ids)
                """, ZonedDateTime.class);
        query.setParameter("dataSourceIds", dataSourceIds);
        return Optional.ofNullable((ZonedDateTime) query.getSingleResult());
    }
}
