package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.bi.BiAggregatedProjectGrantStatsReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;

public interface BiAggregatedProjectGrantStatsReadRepository extends Repository<BiAggregatedProjectGrantStatsReadEntity, ZonedDateTime> {

    @Query(value = """
            SELECT date_trunc(:timeGrouping, d.timestamp) as timestamp,
                   sum(d.total_granted_usd)               as total_granted_usd_amount
            from bi.project_daily_grant_data d
            where d.timestamp >= date_trunc(:timeGrouping, cast(:fromDate as timestamptz))
              and d.timestamp < date_trunc(:timeGrouping, cast(:toDate as timestamptz)) + cast(('1 ' || :timeGrouping) as interval)
            group by 1;
            """, nativeQuery = true)
    List<BiAggregatedProjectGrantStatsReadEntity> findAll(@NonNull String timeGrouping,
                                                          @NonNull ZonedDateTime fromDate,
                                                          @NonNull ZonedDateTime toDate);
}
