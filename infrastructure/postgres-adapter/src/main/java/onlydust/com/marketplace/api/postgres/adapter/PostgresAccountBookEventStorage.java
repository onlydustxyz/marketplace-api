package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookRepository;

import javax.transaction.Transactional;
import java.util.List;

@AllArgsConstructor
public class PostgresAccountBookEventStorage implements AccountBookEventStorage {
    private final @NonNull AccountBookRepository accountBookRepository;

    @Override
    public List<AccountBookEvent> get(Currency currency) {
        return accountBookRepository.findByCurrencyId(currency.id().value())
                .map(AccountBookEntity::getEvents)
                .orElse(List.of())
                .stream().map(AccountBookEventEntity::getEvent)
                .toList();
    }

    @Override
    @Transactional
    public void save(Currency currency, List<AccountBookEvent> pendingEvents) {
        final var accountBookEntity = accountBookRepository.findByCurrencyId(currency.id().value())
                .orElse(AccountBookEntity.of(currency.id().value()));

        pendingEvents.forEach(accountBookEntity::add);
        accountBookRepository.saveAndFlush(accountBookEntity);
    }
}
