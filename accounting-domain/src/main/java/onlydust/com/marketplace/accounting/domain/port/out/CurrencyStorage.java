package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.util.Optional;
import java.util.Set;

public interface CurrencyStorage {
    void save(Currency currency);

    Set<Currency> all();

    Optional<Currency> findByCode(Currency.Code code);

    Optional<Currency> get(Currency.Id id);

    Boolean exists(Currency.Code code);

    Optional<Currency> findByErc20(Blockchain blockchain, String address);
}
