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

import javax.transaction.Transactional;
import java.util.List;

@AllArgsConstructor
public class PostgresAccountBookEventStorage implements AccountBookEventStorage {
    private final @NonNull AccountBookRepository accountBookRepository;
    private final @NonNull AccountBookEventRepository accountBookEventRepository;

    @Override
    public List<IdentifiedAccountBookEvent> getAll(final @NonNull Currency currency) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .map(accountBookEntity -> accountBookEventRepository.findAllByAccountBookId(accountBookEntity.getId()))
                .orElse(List.of())
                .stream().map(AccountBookEventEntity::toIdentifiedAccountBookEvent)
                .toList();
    }

    @Override
    public List<IdentifiedAccountBookEvent> getSince(final @NonNull Currency currency, final long eventId) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .map(accountBookEntity -> accountBookEventRepository.findAllByAccountBookIdAndIdGreaterThanEqual(accountBookEntity.getId(), eventId))
                .orElse(List.of())
                .stream().map(AccountBookEventEntity::toIdentifiedAccountBookEvent)
                .toList();
    }

    @Override
    @Transactional
    public void save(final @NonNull Currency currency, final @NonNull List<IdentifiedAccountBookEvent> pendingEvents) {
        final var accountBookEntity = accountBookRepository.findByCurrencyId(currency.id().value())
                .orElse(AccountBookEntity.of(currency.id().value()));

        accountBookEventRepository.saveAllAndFlush(pendingEvents.stream()
                .map(event -> AccountBookEventEntity.of(accountBookEntity.getId(), event))
                .toList());
    }
}
