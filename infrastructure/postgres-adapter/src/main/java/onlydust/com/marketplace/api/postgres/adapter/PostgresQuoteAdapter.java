package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.QuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.QuoteRepository;

@AllArgsConstructor
public class PostgresQuoteAdapter implements QuoteStorage {
    private final QuoteRepository repository;

    @Override
    public void save(Quote quote) {
        repository.save(QuoteEntity.of(quote));
    }
}
