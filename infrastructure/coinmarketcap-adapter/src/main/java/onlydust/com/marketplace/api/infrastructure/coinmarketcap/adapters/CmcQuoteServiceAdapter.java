package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient.QuoteResponse;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class CmcQuoteServiceAdapter implements QuoteService {
    private final @NonNull CmcClient client;

    @Override
    public Optional<Quote> currentPrice(Currency currency, Currency base) {
        return client.quotes(List.of(currency), List.of(base)).values().stream()
                .findFirst()
                .map(q -> new Quote(currency.id(), base.id(), q.quote().get(client.internalId(base).orElseThrow()).price()));
    }

    @Override
    public List<Quote> currentPrice(List<Currency> currencies, Currency base) {
        final var response = client.quotes(currencies, List.of(base));
        final var baseId = client.internalId(base).orElseThrow();
        return currencies.stream().map(currency -> client.internalId(currency)
                        .flatMap(internalId -> Optional.ofNullable(response.get(internalId)))
                        .map(QuoteResponse::quote)
                        .map(quote -> quote.get(baseId))
                        .map(q -> new Quote(currency.id(), base.id(), q.price()))
                        .orElse(null))
                .toList();
    }
}
