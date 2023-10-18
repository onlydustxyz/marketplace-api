package onlydust.com.marketplace.api.gateways;

import onlydust.com.marketplace.api.domain.gateway.DateProvider;

import java.util.Date;

public class RealDateProvider implements DateProvider {
    @Override
    public Date now() {
        return new Date();
    }
}
