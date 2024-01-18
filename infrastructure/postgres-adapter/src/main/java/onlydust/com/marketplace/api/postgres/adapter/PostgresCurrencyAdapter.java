package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresCurrencyAdapter implements CurrencyStorage {
    private final CurrencyRepository repository;

    @Override
    public void save(Currency currency) {
        repository.save(CurrencyEntity.of(currency));
    }

    @Override
    public List<Currency> all() {
        return repository.findAll().stream().map(CurrencyEntity::toDomain).toList();
    }

    @Override
    public Optional<Currency> findByCode(Currency.Code code) {
        return repository.findByCode(code.toString()).map(CurrencyEntity::toDomain);
    }

    @Override
    public Optional<Currency> get(Currency.Id id) {
        return repository.findById(id.value()).map(CurrencyEntity::toDomain);
    }
}
