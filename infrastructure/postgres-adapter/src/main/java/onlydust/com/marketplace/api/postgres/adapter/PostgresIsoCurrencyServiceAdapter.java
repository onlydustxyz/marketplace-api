package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.out.IsoCurrencyService;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.IsoCurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IsoCurrencyRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresIsoCurrencyServiceAdapter implements IsoCurrencyService {
    final @NonNull IsoCurrencyRepository isoCurrencyRepository;

    @Override
    public Optional<Currency> get(Currency.Code code) {
        return isoCurrencyRepository.findById(code.toString()).map(IsoCurrencyEntity::toCurrency);
    }
}
