package onlydust.com.marketplace.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookProjector;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookTransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Profile("cli")
public class AccountBookRefresh implements CommandLineRunner {
    private final CurrencyStorage currencyStorage;
    private final AccountBookStorage accountBookStorage;
    private final AccountBookEventStorage accountBookEventStorage;
    private final AccountBookProjector accountBookProjector;
    private final AccountBookTransactionRepository accountBookTransactionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (args.length == 0 || !args[0].equals("account_book_refresh")) return;

        accountBookTransactionRepository.deleteAll();
        currencyStorage.all().forEach(this::run);
    }

    private void run(Currency currency) {
        accountBookStorage.get(currency.id())
                .ifPresentOrElse(accountBook -> refresh(accountBook, currency),
                        () -> LOGGER.warn("Account book for currency {} not found", currency.code()));
    }

    private void refresh(AccountBookAggregate accountBook, Currency currency) {
        LOGGER.info("Refreshing account book for currency {}", currency.code());
        final var events = accountBookEventStorage.getAll(currency);

        UnsafeAccountBook.of(accountBook)
                .observed(accountBookProjector)
                .emit(events);
    }

    private static class UnsafeAccountBook extends AccountBookAggregate {
        private UnsafeAccountBook(AccountBookAggregate.Id id) {
            super(id);
        }

        public static UnsafeAccountBook of(AccountBookAggregate accountBook) {
            return new UnsafeAccountBook(accountBook.id());
        }

        public UnsafeAccountBook observed(AccountBookObserver observer) {
            super.observed(observer);
            return this;
        }

        public void emit(List<IdentifiedAccountBookEvent> events) {
            events
                    .stream()
                    .map(IdentifiedAccountBookEvent::data)
                    .forEach(this::emit);
        }
    }
}
