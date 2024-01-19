package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyStorage {
    void save(Currency currency);

    List<Currency> all();

    Optional<Currency> findByCode(Currency.Code code);

    Optional<Currency> get(Currency.Id id);

    Boolean exists(Currency.Code code);
}
