package onlydust.com.marketplace.api.domain.mocks;

import lombok.Setter;
import onlydust.com.marketplace.api.domain.gateway.DateProvider;

import java.util.Date;

@Setter
public class DeterministicDateProvider implements DateProvider {

    private Date now;

    @Override
    public Date now() {
        return now;
    }
}
