package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.bi.BiTimestampSeriesReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;

public interface BiTimestampSeriesReadRepository extends Repository<BiTimestampSeriesReadEntity, ZonedDateTime> {

    @Query(value = """
            SELECT generate_series(date_trunc(:timeGrouping, cast(:fromDate as timestamptz)),
                                   date_trunc(:timeGrouping, cast(:toDate as timestamptz)),
                                   cast(('1 ' || :timeGrouping) as interval)) AS timestamp
            """, nativeQuery = true)
    List<BiTimestampSeriesReadEntity> generateSeries(@NonNull String timeGrouping,
                                                     @NonNull ZonedDateTime fromDate,
                                                     @NonNull ZonedDateTime toDate);
}
