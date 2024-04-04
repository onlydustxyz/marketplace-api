package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresAccountBookEventAdapter implements AccountBookEventStorage {
    private final @NonNull AccountBookRepository accountBookRepository;
    private final @NonNull AccountBookEventRepository accountBookEventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<IdentifiedAccountBookEvent> getAll(final @NonNull Currency currency) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .map(accountBookEntity -> accountBookEventRepository.findAllByAccountBookId(accountBookEntity.getId()))
                .orElse(List.of())
                .stream().map(AccountBookEventEntity::toIdentifiedAccountBookEvent)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IdentifiedAccountBookEvent> getSince(final @NonNull Currency currency, final long eventId) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .map(accountBookEntity -> accountBookEventRepository.findAllByAccountBookIdAndIdGreaterThanEqualOrderByIdAsc(accountBookEntity.getId(),
                        eventId))
                .orElse(List.of())
                .stream().map(AccountBookEventEntity::toIdentifiedAccountBookEvent)
                .toList();
    }

    @Override
    @Transactional
    public void save(final @NonNull Currency currency, final @NonNull List<IdentifiedAccountBookEvent> pendingEvents) {
        final var accountBookEntity = accountBookRepository.findByCurrencyId(currency.id().value())
                .orElseGet(() -> accountBookRepository.saveAndFlush(AccountBookEntity.of(currency.id().value())));

        pendingEvents.stream()
                .map(event -> AccountBookEventEntity.of(accountBookEntity.getId(), event))
                .forEach(accountBookEventRepository::insert);
    }

    @Override
    public Optional<Long> getLastEventId(Currency currency) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .flatMap(accountBookEntity -> accountBookEventRepository.findFirstByAccountBookIdOrderByIdDesc(accountBookEntity.getId()).map(AccountBookEventEntity::id));
    }
}
