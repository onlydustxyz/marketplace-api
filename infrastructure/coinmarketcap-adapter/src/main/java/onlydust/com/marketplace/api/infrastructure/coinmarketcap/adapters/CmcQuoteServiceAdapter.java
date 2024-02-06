package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class CmcQuoteServiceAdapter implements QuoteService {
    private final @NonNull CmcClient client;

    @Override
    public List<Quote> currentPrice(Set<Currency> currencies, Set<Currency> bases) {
        final var response = client.quotes(currencies, bases);
        final var quotes = new ArrayList<Quote>();

        for (Currency currency : currencies) {
            final var currencyId = client.internalId(currency);
            if (currencyId.isEmpty()) {
                continue;
            }

            for (Currency base : bases) {
                final var baseId = client.internalId(base);
                if (baseId.isEmpty())
                    continue;

                Optional.ofNullable(response.get(currencyId.get()))
                        .flatMap(q -> Optional.ofNullable(q.quote().get(baseId.get())))
                        .ifPresent(p -> quotes.add(new Quote(currency.id(), base.id(), p.price())));
            }
        }

        return quotes;
    }
}
