package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.TransferEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.api.postgres.adapter.PostgresAccountBookEventStorage;
import onlydust.com.marketplace.api.postgres.adapter.PostgresCurrencyAdapter;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresAccountBookEventStorageIT extends AbstractPostgresIT {
    @Autowired
    private PostgresAccountBookEventStorage postgresAccountBookEventStorage;
    @Autowired
    private PostgresCurrencyAdapter postgresCurrencyAdapter;

    final Currency currency = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);

    @BeforeEach
    void setUp() {
        postgresCurrencyAdapter.save(currency);
    }

    @Test
    @Transactional
    void should_return_empy_list_when_not_found() {
        assertThat(postgresAccountBookEventStorage.getAll(currency)).isEmpty();
    }

    @SneakyThrows
    @Test
    void should_save_and_get_events() {
        final List<IdentifiedAccountBookEvent> events1 = List.of(
                IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(SponsorAccount.Id.random()), PositiveAmount.of(100L))),
                IdentifiedAccountBookEvent.of(2, new MintEvent(AccountId.of(SponsorAccount.Id.random()), PositiveAmount.of(100L)))
        );

        final List<IdentifiedAccountBookEvent> events2 = List.of(
                IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(SponsorAccount.Id.random()), AccountId.of(SponsorAccount.Id.random()),
                        PositiveAmount.of(100L))),
                IdentifiedAccountBookEvent.of(4, new TransferEvent(AccountId.of(SponsorAccount.Id.random()), AccountId.of(SponsorAccount.Id.random()),
                        PositiveAmount.of(100L)))
        );

        postgresAccountBookEventStorage.save(currency, events1);
        postgresAccountBookEventStorage.save(currency, events2);

        final var allEvents = Stream.of(events1, events2).flatMap(List::stream).toList();
        assertThat(postgresAccountBookEventStorage.getAll(currency)).containsExactlyElementsOf(allEvents);
        assertThat(postgresAccountBookEventStorage.getSince(currency, 1L)).containsExactlyElementsOf(allEvents);
        assertThat(postgresAccountBookEventStorage.getSince(currency, 3L)).containsExactlyElementsOf(events2);
    }
}
