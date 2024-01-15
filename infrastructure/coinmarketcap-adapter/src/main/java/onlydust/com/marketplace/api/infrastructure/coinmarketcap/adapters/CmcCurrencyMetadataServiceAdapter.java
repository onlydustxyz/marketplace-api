package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyMetadataService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;

import java.util.Optional;

@AllArgsConstructor
public class CmcCurrencyMetadataServiceAdapter implements CurrencyMetadataService {
    private final CmcClient client;

    @Override
    public Optional<Currency.Metadata> get(ERC20 token) {
        return client.metadata(token).map(m -> new Currency.Metadata(m.description(), m.logo()));
    }
}
