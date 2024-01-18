package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.util.Optional;

public interface IsoCurrencyService {
    Optional<Currency> get(Currency.Code code);
}
