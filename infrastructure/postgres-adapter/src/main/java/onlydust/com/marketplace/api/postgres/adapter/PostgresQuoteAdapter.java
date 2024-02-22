package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.HistoricalQuoteRepository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

@AllArgsConstructor
public class PostgresQuoteAdapter implements QuoteStorage {
    private final HistoricalQuoteRepository repository;

    @Override
    public void save(Collection<Quote> quotes) {
        repository.saveAll(quotes.stream().map(HistoricalQuoteEntity::of).toList());
    }

    @Override
    public Optional<Quote> nearest(Currency.Id baseId, Currency.Id targetId, ZonedDateTime date) {
        return repository.findFirstByBaseIdAndTargetIdAndTimestampLessThanEqualOrderByTimestampDesc(
                        baseId.value(),
                        targetId.value(),
                        date.toInstant())
                .map(HistoricalQuoteEntity::toDomain);
    }
}
