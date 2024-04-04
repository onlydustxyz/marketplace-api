package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.TransferEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.api.postgres.adapter.PostgresAccountBookEventAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresCurrencyAdapter;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PostgresAccountBookEventAdapterIT extends AbstractPostgresIT {
    @Autowired
    private PostgresAccountBookEventAdapter postgresAccountBookEventAdapter;
    @Autowired
    private PostgresCurrencyAdapter postgresCurrencyAdapter;

    @Test
    void should_return_empty_list_when_not_found() {
        final var currency = newCurrency("FOOBAR1");
        assertThat(postgresAccountBookEventAdapter.getAll(currency)).isEmpty();
        assertThat(postgresAccountBookEventAdapter.getLastEventId(currency)).isEmpty();
    }


    @SneakyThrows
    @Test
    void should_save_and_get_events() {
        final var currency = newCurrency("FOOBAR2");

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

        postgresAccountBookEventAdapter.insert(currency, events1);
        assertThat(postgresAccountBookEventAdapter.getLastEventId(currency).get()).isEqualTo(2L);
        postgresAccountBookEventAdapter.insert(currency, events2);
        assertThat(postgresAccountBookEventAdapter.getLastEventId(currency).get()).isEqualTo(4L);

        final var allEvents = Stream.of(events1, events2).flatMap(List::stream).toList();
        assertThat(postgresAccountBookEventAdapter.getAll(currency)).containsExactlyElementsOf(allEvents);
        assertThat(postgresAccountBookEventAdapter.getSince(currency, 1L)).containsExactlyElementsOf(allEvents);
        assertThat(postgresAccountBookEventAdapter.getSince(currency, 3L)).containsExactlyElementsOf(events2);
    }

    @Test
    void should_fail_to_save_events_with_same_id() {
        final var currency = newCurrency("FOOBAR3");

        final List<IdentifiedAccountBookEvent> events1 = List.of(
                IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(SponsorAccount.Id.random()), PositiveAmount.of(100L))),
                IdentifiedAccountBookEvent.of(2, new MintEvent(AccountId.of(SponsorAccount.Id.random()), PositiveAmount.of(100L)))
        );

        final List<IdentifiedAccountBookEvent> events2 = List.of(
                IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(SponsorAccount.Id.random()), AccountId.of(SponsorAccount.Id.random()),
                        PositiveAmount.of(100L))),
                IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(SponsorAccount.Id.random()), AccountId.of(SponsorAccount.Id.random()),
                        PositiveAmount.of(100L)))
        );

        postgresAccountBookEventAdapter.insert(currency, events1);
        assertThat(postgresAccountBookEventAdapter.getLastEventId(currency).get()).isEqualTo(2L);
        assertThat(postgresAccountBookEventAdapter.getAll(currency)).containsExactlyElementsOf(events1);

        assertThatThrownBy(() -> postgresAccountBookEventAdapter.insert(currency, events2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("constraint [account_books_events_pkey]");

        assertThat(postgresAccountBookEventAdapter.getLastEventId(currency).get()).isEqualTo(2L);
        assertThat(postgresAccountBookEventAdapter.getAll(currency)).containsExactlyElementsOf(events1);
    }

    @NotNull
    private Currency newCurrency(String code) {
        if (!postgresCurrencyAdapter.exists(Currency.Code.of(code)))
            postgresCurrencyAdapter.save(Currency.crypto(code, Currency.Code.of(code), 18));
        return postgresCurrencyAdapter.findByCode(Currency.Code.of(code)).get();
    }
}
