package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.project.domain.port.output.ProjectCurrencyStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PostgresCurrencyAdapter implements CurrencyStorage, ProjectCurrencyStoragePort {
    private final CurrencyRepository repository;

    @Override
    public void save(Currency currency) {
        repository.saveAndFlush(CurrencyEntity.of(currency));
    }

    @Override
    public Set<Currency> all() {
        return repository.findAll().stream().map(CurrencyEntity::toDomain).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Currency> findByCode(Currency.Code code) {
        return repository.findByCode(code.toString()).map(CurrencyEntity::toDomain);
    }

    @Override
    public Optional<Currency> get(Currency.Id id) {
        return repository.findById(id.value()).map(CurrencyEntity::toDomain);
    }

    @Override
    public Boolean exists(Currency.Code code) {
        return repository.existsByCode(code.toString());
    }

    @Override
    public Optional<Currency> findByErc20(final @NonNull Blockchain blockchain, final @NonNull String address) {
        return repository.findByErc20(NetworkEnumEntity.of(blockchain), address).map(CurrencyEntity::toDomain);
    }

    @Override
    public Optional<UUID> findCurrencyIdByCode(String code) {
        return repository.findByCode(code).map(CurrencyEntity::id);
    }
}
