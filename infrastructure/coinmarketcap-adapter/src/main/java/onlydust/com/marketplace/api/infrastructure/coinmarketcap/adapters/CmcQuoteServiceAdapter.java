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
    public List<Quote> currentPrice(Set<Currency> currencies, Set<Currency> targets) {
        final var response = client.quotes(currencies, targets);
        final var quotes = new ArrayList<Quote>();

        for (Currency currency : currencies) {
            for (Currency base : targets) {
                Optional.ofNullable(response.get(currency.cmcId()))
                        .flatMap(q -> Optional.ofNullable(q.quote().get(base.cmcId())))
                        .filter(p -> p.price() != null)
                        .ifPresent(p -> quotes.add(new Quote(currency.id(), base.id(), p.price(), p.lastUpdated().toInstant())));
            }
        }

        return quotes;
    }
}
