package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import static java.util.Objects.isNull;

public interface DateMapper {

    ZonedDateTime DEFAULT_FROM_DATE = ZonedDateTime.parse("2007-10-20T05:24:19Z");

    static ZonedDateTime toZoneDateTime(final Date date) {
        return isNull(date) ? null : ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }

    static Date parse(@NonNull final String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            throw OnlyDustException.badRequest("Invalid date format", e);
        }
    }

    static Date parseNullable(final String date) {
        return isNull(date) ? null : parse(date);
    }

    static ZonedDateTime parseZonedNullable(final String date) {
        return toZoneDateTime(parseNullable(date));
    }

    static ZonedDateTime sanitizedDate(String fromDate, ZonedDateTime defaultFromDate) {
        return Optional.ofNullable(DateMapper.parseNullable(fromDate)).map(DateMapper::toZoneDateTime).orElse(defaultFromDate);
    }
}
