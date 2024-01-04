package onlydust.com.marketplace.api.domain.mocks;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import lombok.Setter;
import onlydust.com.marketplace.api.domain.gateway.DateProvider;

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
