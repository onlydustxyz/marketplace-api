package onlydust.com.marketplace.kernel.mapper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateMapper {
    public static Date ofNullable(ZonedDateTime date) {
        return date != null ? Date.from(date.toInstant()) : null;
    }

    public static ZonedDateTime ofNullable(Date date) {
        return date != null ? ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS) : null;
    }

    public static ZonedDateTime ofNullable(Instant instant) {
        return instant != null ? ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS) : null;
    }
}
