package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.QuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.QuoteRepository;

import java.util.Collection;

@AllArgsConstructor
public class PostgresQuoteAdapter implements QuoteStorage {
    private final QuoteRepository repository;

    @Override
    public void save(Collection<Quote> quote) {
        repository.saveAll(quote.stream().map(QuoteEntity::of).toList());
    }
}
