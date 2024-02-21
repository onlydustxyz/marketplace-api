package onlydust.com.marketplace.project.domain.mocks;

import lombok.Setter;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Setter
public class DeterministicDateProvider implements DateProvider {

    private Date now;

    public DeterministicDateProvider() {
        this.now = Date.from(ZonedDateTime.of(2023, 4, 1, 10, 5, 32, 0, ZoneId.of("UTC")).toInstant());
    }

    @Override
    public Date now() {
        return now;
    }
}
