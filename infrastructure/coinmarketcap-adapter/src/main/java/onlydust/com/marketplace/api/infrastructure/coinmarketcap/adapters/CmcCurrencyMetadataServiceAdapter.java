package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyMetadataService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;

import java.util.Optional;

@AllArgsConstructor
public class CmcCurrencyMetadataServiceAdapter implements CurrencyMetadataService {
    private final CmcClient client;

    @Override
    public Optional<Currency.Metadata> get(@NonNull ERC20 token) {
        return client.metadata(token).map(m -> new Currency.Metadata(m.id(), m.name(), m.description(), m.logo()));
    }

    @Override
    public Optional<Currency.Metadata> get(@NonNull Currency.Code code) {
        return client.metadata(code).map(m -> new Currency.Metadata(m.id(), m.name(), m.description(), m.logo()));
    }

    @Override
    public int fiatId(@NonNull Currency.Code code) {
        return client.fiatId(code);
    }
}
