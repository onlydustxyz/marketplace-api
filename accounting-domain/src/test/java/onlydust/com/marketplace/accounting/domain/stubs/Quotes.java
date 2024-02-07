package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.math.BigDecimal;
import java.time.Instant;

public interface Quotes {
    Instant TIMESTAMP = Instant.parse("2024-01-01T00:00:00Z");
    Quote USDC_USD = new Quote(Currencies.USDC.id(), Currencies.USD.id(), BigDecimal.valueOf(0.35), TIMESTAMP);
    Quote USDC_USDC = new Quote(Currencies.USDC.id(), Currencies.USDC.id(), BigDecimal.ONE, TIMESTAMP);
    Quote LORDS_USD = new Quote(Currencies.LORDS.id(), Currencies.USD.id(), BigDecimal.valueOf(0.35), TIMESTAMP);
}
