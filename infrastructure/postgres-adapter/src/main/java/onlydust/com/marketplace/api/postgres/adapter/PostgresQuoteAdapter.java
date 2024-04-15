package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LatestQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldestQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.HistoricalQuoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.LatestQuoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.OldestQuoteRepository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

@AllArgsConstructor
public class PostgresQuoteAdapter implements QuoteStorage {
    private final HistoricalQuoteRepository repository;
    private final LatestQuoteRepository latestQuoteRepository;
    private final OldestQuoteRepository oldestQuoteRepository;

    @Override
    public void save(Collection<Quote> quotes) {
        repository.saveAllAndFlush(quotes.stream().map(HistoricalQuoteEntity::of).toList());
        quotes.forEach(quote -> {
            var latestQuote = latestQuoteRepository.findById(new LatestQuoteEntity.PrimaryKey(quote.base().value(), quote.target().value()));
            if (latestQuote.isEmpty() || latestQuote.get().timestamp().isBefore(quote.timestamp())) {
                latestQuoteRepository.saveAndFlush(LatestQuoteEntity.of(quote));
            }
        });
        quotes.forEach(quote -> {
            var oldestQuote = oldestQuoteRepository.findById(new OldestQuoteEntity.PrimaryKey(quote.base().value(), quote.target().value()));
            if (oldestQuote.isEmpty() || oldestQuote.get().timestamp().isAfter(quote.timestamp())) {
                oldestQuoteRepository.saveAndFlush(OldestQuoteEntity.of(quote));
            }
        });
    }

    @Override
    public Optional<Quote> nearest(Currency.Id baseId, Currency.Id targetId, ZonedDateTime date) {
        return repository.findFirstByBaseIdAndTargetIdAndTimestampLessThanEqualOrderByTimestampDesc(
                        baseId.value(),
                        targetId.value(),
                        date.toInstant())
                .map(HistoricalQuoteEntity::toDomain);
    }

    @Override
    public Optional<Quote> latest(Currency.Id baseId, Currency.Id targetId) {
        return latestQuoteRepository.findById(new LatestQuoteEntity.PrimaryKey(baseId.value(), targetId.value()))
                .map(LatestQuoteEntity::toDomain);
    }
}
