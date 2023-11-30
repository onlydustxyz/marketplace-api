package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    static Date parse(final String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
}
