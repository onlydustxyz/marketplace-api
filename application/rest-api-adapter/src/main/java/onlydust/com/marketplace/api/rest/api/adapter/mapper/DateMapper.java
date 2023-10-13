package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.isNull;

public interface DateMapper {

    static ZonedDateTime toZoneDateTime(final Date date) {
        return isNull(date) ? null : ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }
}
