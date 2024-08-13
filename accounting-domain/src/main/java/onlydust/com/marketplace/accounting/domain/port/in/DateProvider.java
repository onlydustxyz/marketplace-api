package onlydust.com.marketplace.accounting.domain.port.in;

import java.time.ZonedDateTime;

public interface DateProvider {
    ZonedDateTime now();
}
