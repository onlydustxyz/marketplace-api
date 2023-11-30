package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import org.apache.commons.lang3.time.DateParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.util.Objects.isNull;

public interface DateMapper {

    static ZonedDateTime toZoneDateTime(final Date date) {
        return isNull(date) ? null : ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }

    static Instant parseInstant(final String date) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(date, Instant::from);
    }
}
