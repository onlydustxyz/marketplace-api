package onlydust.com.marketplace.api.read.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.TimeGroupingEnum;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedProjectKpisReadEntity;
import org.intellij.lang.annotations.Language;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class AggregatedProjectKpisReadRepository {
    private final EntityManager entityManager;

    @Language("PostgreSQL")
    private final static String SELECT_QUERY = """
            WITH aggregated_project_stats AS
                     (SELECT d.#timeGrouping#_timestamp                                                                               as timestamp,
                             count(distinct d.project_id)                                                                 as active_project_count,
            
                             count(distinct d.project_id)
                             filter (where previous.timestamp is null)                                                    as new_project_count,
            
                             count(distinct d.project_id)
                             filter (where previous.timestamp < d.#timeGrouping#_timestamp - cast(:timeGroupingInterval as interval)) as reactivated_project_count,
            
                             count(d.contribution_id)
                             filter ( where d.is_merged_pr = 1 )                                                              as merged_pr_count
                      from bi.contribution_data d
                               join bi.project_global_data p on d.project_id = p.project_id
                               left join lateral ( select max(previous.#timeGrouping#_timestamp) as timestamp
                                                   from bi.contribution_data previous
                                                        join bi.project_global_data p on previous.project_id = p.project_id
                                                   where previous.project_id = d.project_id
                                                     and previous.#timeGrouping#_timestamp < d.#timeGrouping#_timestamp
                                                     and (coalesce(:programOrEcosystemIds) is null
                                                       or cast(:programOrEcosystemIds as uuid[]) && p.program_ids
                                                       or cast(:programOrEcosystemIds as uuid[]) && p.ecosystem_ids)) previous on true
                      where
                        -- We need to get one interval before fromDate to calculate churned project count
                          d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz)) - cast(:timeGroupingInterval as interval)
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:programOrEcosystemIds) is null
                          or cast(:programOrEcosystemIds as uuid[]) && p.program_ids
                          or cast(:programOrEcosystemIds as uuid[]) && p.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_rewards_stats AS
                     (SELECT d.#timeGrouping#_timestamp    as timestamp,
                             sum(d.usd_amount) as total_rewarded_usd_amount
                      from bi.reward_data d
                            join bi.project_global_data p on d.project_id = p.project_id
                      where d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
                        and (coalesce(:programOrEcosystemIds) is null
                          or cast(:programOrEcosystemIds as uuid[]) && p.program_ids
                          or cast(:programOrEcosystemIds as uuid[]) && p.ecosystem_ids)
                      group by 1),
            
                 aggregated_project_grants_stats AS
                     (SELECT d.#timeGrouping#_timestamp    as timestamp,
                             sum(d.usd_amount) as total_granted_usd_amount
                      from bi.project_grants_data d
                      where d.#timeGrouping#_timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
                        and d.#timeGrouping#_timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(:timeGroupingInterval as interval)
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
                   aps.active_project_count,
                   aps.new_project_count,
                   aps.reactivated_project_count,
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
            """;

    public List<AggregatedProjectKpisReadEntity> findAll(@NonNull TimeGroupingEnum timeGrouping,
                                                         @NonNull String timeGroupingInterval,
                                                         @NonNull ZonedDateTime fromDate,
                                                         @NonNull ZonedDateTime toDate,
                                                         UUID[] programOrEcosystemIds) {
        final var timestampColumnPrefix = timeGrouping.name().toLowerCase();
        final var query = entityManager.createNativeQuery(SELECT_QUERY.replaceAll("#timeGrouping#", timestampColumnPrefix),
                AggregatedProjectKpisReadEntity.class);
        query.setParameter("timeGrouping", timeGrouping.name());
        query.setParameter("timeGroupingInterval", timeGroupingInterval);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("programOrEcosystemIds", programOrEcosystemIds);
        return query.getResultList();
    }
}
