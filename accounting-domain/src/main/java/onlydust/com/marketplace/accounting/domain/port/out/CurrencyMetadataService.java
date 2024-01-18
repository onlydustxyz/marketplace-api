package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;

import java.util.Optional;

public interface CurrencyMetadataService {
    Optional<Currency.Metadata> get(ERC20 token);

    Optional<Currency.Metadata> get(Currency.Code code);
}
