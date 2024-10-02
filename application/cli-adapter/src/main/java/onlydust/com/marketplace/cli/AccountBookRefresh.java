package onlydust.com.marketplace.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookProjector;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.api.postgres.adapter.repository.AllTransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Profile("cli")
public class AccountBookRefresh implements CommandLineRunner {
    private final CurrencyStorage currencyStorage;
    private final AccountBookStorage accountBookStorage;
    private final AccountBookEventStorage accountBookEventStorage;
    private final AccountBookProjector accountBookProjector;
    private final AllTransactionRepository allTransactionRepository;
    private final StopWatch stopWatch = new StopWatch();

    @Override
    @Transactional
    public void run(String... args) {
        if (args.length == 0 || !args[0].equals("account_book_refresh")) return;

        stopWatch.start();
        allTransactionRepository.deleteAll();
        currencyStorage.all().forEach(this::run);
        stopWatch.stop();
        LOGGER.info("Account book refresh completed in {} ms", stopWatch.getTotalTimeMillis());
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
                .refresh(events, accountBookProjector);
    }

    private static class UnsafeAccountBook extends AccountBookAggregate {
        private UnsafeAccountBook(AccountBookAggregate.Id id) {
            super(id);
        }

        public static UnsafeAccountBook of(AccountBookAggregate accountBook) {
            return new UnsafeAccountBook(accountBook.id());
        }

        public void refresh(List<IdentifiedAccountBookEvent> events, AccountBookProjector projector) {
            events.forEach(e -> {
                final var transactions = receive((IdentifiedAccountBookEvent<List<Transaction>>) e);
                transactions.forEach(t -> projector.on(id(), e.timestamp(), t));
            });
        }
    }
}
