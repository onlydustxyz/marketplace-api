package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.bi.ContributorActivityGraphDayReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ContributorActivityGraphReadRepository extends Repository<ContributorActivityGraphDayReadEntity,
        ContributorActivityGraphDayReadEntity.PrimaryKey> {

    @Query(value = """
            WITH aggregated_contributor_stats AS
                     (SELECT d.day_timestamp                                                                                                          as day_timestamp,
                             count(d.contribution_uuid) filter ( where d.contribution_type = 'ISSUE' and d.contribution_status = 'COMPLETED' )        as issue_count,
                             count(d.contribution_uuid) filter ( where d.contribution_type = 'PULL_REQUEST' and d.contribution_status = 'COMPLETED' ) as pr_count,
                             count(d.contribution_uuid) filter ( where d.contribution_type = 'CODE_REVIEW' and d.contribution_status = 'COMPLETED' )  as code_review_count
                      from bi.p_per_contributor_contribution_data d
                      where d.day_timestamp >= date_trunc('DAY', cast(:fromDate as timestamptz))
                        and d.contributor_id = :contributorId
                        and (:onlyOnlyDustData is false or d.project_id is not null)
                        and (coalesce(:projectIds) is null or d.project_id in (:projectIds))
                      group by 1),
            
                 aggregated_project_rewards_stats AS
                     (SELECT d.day_timestamp    as day_timestamp,
                             count(d.reward_id) as reward_count
                      from bi.p_reward_data d
                      where d.day_timestamp >= date_trunc('DAY', cast(:fromDate as timestamptz))
                        and d.contributor_id = :contributorId
                        and (:onlyOnlyDustData is false or d.project_id is not null)
                        and (coalesce(:projectIds) is null or d.project_id in (:projectIds))
                      group by 1),
            
                 all_timestamps_to_return AS
                     (SELECT series.day_timestamp as day_timestamp
                      FROM (select generate_series(date_trunc('DAY', cast(:fromDate as timestamptz)),
                                                   date_trunc('DAY', now()),
                                                   interval '1 day') as day_timestamp) series)
            
            SELECT extract('DOY' from allt.day_timestamp)  as day,
                   extract('WEEK' from allt.day_timestamp) as week,
                   extract('YEAR' from allt.day_timestamp) as year,
                   coalesce(acs.issue_count, 0)            as issue_count,
                   coalesce(acs.pr_count, 0)               as pr_count,
                   coalesce(acs.code_review_count, 0)      as code_review_count,
                   coalesce(aprs.reward_count, 0)          as reward_count
            FROM all_timestamps_to_return allt
                     LEFT JOIN aggregated_contributor_stats acs
                               ON allt.day_timestamp = acs.day_timestamp
                     LEFT JOIN aggregated_project_rewards_stats aprs
                               ON allt.day_timestamp = aprs.day_timestamp
            """,
            nativeQuery = true)
    List<ContributorActivityGraphDayReadEntity> findLastYear(ZonedDateTime fromDate,
                                                             Long contributorId,
                                                             Boolean onlyOnlyDustData,
                                                             List<UUID> projectIds);

}
