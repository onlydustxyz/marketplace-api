package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresAccountBookEventAdapter implements AccountBookEventStorage {
    private final @NonNull AccountBookRepository accountBookRepository;
    private final @NonNull AccountBookEventRepository accountBookEventRepository;

    @Override
    @Transactional(readOnly = true)
    public @NotNull List<IdentifiedAccountBookEvent> getAll(final @NonNull Currency currency) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .map(accountBookEntity -> accountBookEventRepository.findAllByAccountBookIdOrderByIdAsc(accountBookEntity.getId()))
                .orElse(List.of())
                .stream().map(AccountBookEventEntity::toIdentifiedAccountBookEvent)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public @NotNull List<IdentifiedAccountBookEvent> getSince(final @NonNull Currency currency, final long eventId) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .map(accountBookEntity -> accountBookEventRepository.findAllByAccountBookIdAndIdGreaterThanEqualOrderByIdAsc(accountBookEntity.getId(),
                        eventId))
                .orElse(List.of())
                .stream().map(AccountBookEventEntity::toIdentifiedAccountBookEvent)
                .toList();
    }

    @Override
    @Transactional
    public void insert(final @NonNull AccountBookAggregate.Id accountBookId,
                       final @NonNull Currency currency,
                       final @NonNull List<IdentifiedAccountBookEvent> pendingEvents) {
        final var accountBookEntity = accountBookRepository.findByCurrencyId(currency.id().value())
                .orElseGet(() -> accountBookRepository.saveAndFlush(AccountBookEntity.of(accountBookId.value(), currency.id().value())));

        pendingEvents.stream()
                .map(event -> AccountBookEventEntity.of(accountBookEntity.getId(), event))
                .forEach(accountBookEventRepository::insert);
    }

    @Override
    @Transactional(readOnly = true)
    public @NotNull Optional<Long> getLastEventId(@NotNull Currency currency) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .flatMap(accountBookEntity -> accountBookEventRepository.findFirstByAccountBookIdOrderByIdDesc(accountBookEntity.getId()).map(AccountBookEventEntity::id));
    }
}
