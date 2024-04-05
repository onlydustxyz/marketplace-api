package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.exception.EventSequenceViolationException;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CachedAccountBookProviderTest {
    final Currency currency = Currencies.USDC;
    AccountBookEventStorageStub accountBookEventStorage;
    CachedAccountBookProvider cachedAccountBookProvider;

    @BeforeEach
    void setUp() {
        accountBookEventStorage = mock(AccountBookEventStorageStub.class);
        cachedAccountBookProvider = new CachedAccountBookProvider(accountBookEventStorage);
    }

    @Test
    void should_get_new_empty_account_book() {
        // When
        when(accountBookEventStorage.getSince(currency, 1)).thenReturn(List.of());
        final var accountBook = cachedAccountBookProvider.get(currency);

        // Then
        verify(accountBookEventStorage).getSince(currency, 1);
        assertThat(accountBook).isNotNull();
        assertThat(accountBook.getAndClearPendingEvents()).isEmpty();
    }

    @Test
    void should_get_existing_account_book_not_in_cache() {
        // When
        final var sponsorAccountId = AccountBook.AccountId.of(SponsorAccount.Id.random());
        when(accountBookEventStorage.getSince(currency, 1)).thenReturn(List.of(
                IdentifiedAccountBookEvent.of(1, new AccountBookAggregate.MintEvent(sponsorAccountId, PositiveAmount.of(1L))),
                IdentifiedAccountBookEvent.of(2, new AccountBookAggregate.MintEvent(sponsorAccountId, PositiveAmount.of(5L)))
        ));
        final var accountBook = cachedAccountBookProvider.get(currency);

        // Then
        verify(accountBookEventStorage).getSince(currency, 1);
        assertThat(accountBook).isNotNull();
        assertThat(accountBook.getAndClearPendingEvents()).isEmpty();
        assertThat(accountBook.state().balanceOf(sponsorAccountId)).isEqualTo(PositiveAmount.of(6L));
    }

    private AccountBookAggregate givenAccountBookInCache(AccountBook.AccountId sponsorAccountId) {
        when(accountBookEventStorage.getSince(currency, 1)).thenReturn(List.of(
                IdentifiedAccountBookEvent.of(1, new AccountBookAggregate.MintEvent(sponsorAccountId, PositiveAmount.of(1L))),
                IdentifiedAccountBookEvent.of(2, new AccountBookAggregate.MintEvent(sponsorAccountId, PositiveAmount.of(5L)))
        ));
        final var initialAccountBook = cachedAccountBookProvider.get(currency);
        verify(accountBookEventStorage).getSince(currency, 1);
        assertThat(initialAccountBook.state().balanceOf(sponsorAccountId)).isEqualTo(PositiveAmount.of(6L));
        return initialAccountBook;
    }

    @Test
    void should_get_existing_account_book_in_cache_not_up_to_date() {
        // Given
        final var sponsorAccountId = AccountBook.AccountId.of(SponsorAccount.Id.random());
        final var initialAccountBook = givenAccountBookInCache(sponsorAccountId);

        // When
        when(accountBookEventStorage.getSince(currency, 3)).thenReturn(List.of(
                IdentifiedAccountBookEvent.of(3, new AccountBookAggregate.MintEvent(sponsorAccountId, PositiveAmount.of(2L))),
                IdentifiedAccountBookEvent.of(4, new AccountBookAggregate.MintEvent(sponsorAccountId, PositiveAmount.of(8L)))
        ));
        final var accountBook = cachedAccountBookProvider.get(currency);

        // Then
        verify(accountBookEventStorage).getSince(currency, 3);
        assertThat(accountBook).isSameAs(initialAccountBook);
        assertThat(accountBook).isNotNull();
        assertThat(accountBook.getAndClearPendingEvents()).isEmpty();
        assertThat(accountBook.state().balanceOf(sponsorAccountId)).isEqualTo(PositiveAmount.of(16L));
    }

    @Test
    void should_get_existing_account_book_in_cache_up_to_date() {
        // Given
        final var sponsorAccountId = AccountBook.AccountId.of(SponsorAccount.Id.random());
        final var initialAccountBook = givenAccountBookInCache(sponsorAccountId);

        // When
        when(accountBookEventStorage.getSince(currency, 3)).thenReturn(List.of());
        final var accountBook = cachedAccountBookProvider.get(currency);

        // Then
        verify(accountBookEventStorage).getSince(currency, 3);
        assertThat(accountBook).isSameAs(initialAccountBook);
        assertThat(accountBook).isNotNull();
        assertThat(accountBook.getAndClearPendingEvents()).isEmpty();
        assertThat(accountBook.state().balanceOf(sponsorAccountId)).isEqualTo(PositiveAmount.of(6L));
    }

    @Test
    void should_not_save_anything_when_no_pending_event() throws EventSequenceViolationException {
        // Given
        when(accountBookEventStorage.getSince(currency, 1)).thenReturn(List.of());
        final var accountBook = cachedAccountBookProvider.get(currency);

        // When
        cachedAccountBookProvider.save(currency, accountBook);

        // Then
        verify(accountBookEventStorage, never()).insert(any(), anyList());
        assertThat(cachedAccountBookProvider.get(currency)).isSameAs(accountBook);
    }

    @Test
    void should_prevent_saving_and_invalidate_account_book_when_events_are_not_strictly_sequential() throws EventSequenceViolationException {
        // Given
        final var sponsorAccountId = AccountBook.AccountId.of(SponsorAccount.Id.random());
        final var accountBook = givenAccountBookInCache(sponsorAccountId);
        accountBook.mint(sponsorAccountId, PositiveAmount.of(8L));
        assertThat(accountBook.pendingEvents()).hasSize(1);

        // When
        when(accountBookEventStorage.getLastEventId(currency)).thenReturn(Optional.of(1L));
        assertThatThrownBy(() -> cachedAccountBookProvider.save(currency, accountBook))
                .isInstanceOf(OnlyDustException.class)
                .hasCauseInstanceOf(EventSequenceViolationException.class)
                .hasMessageContaining("Could not save account book")
                .hasStackTraceContaining("Expected next event id to be 2 but got 3. Event ids must be strictly sequential.");

        // Then
        verify(accountBookEventStorage, never()).insert(any(), anyList());
        assertThat(cachedAccountBookProvider.get(currency)).isNotSameAs(accountBook);
    }

    @Test
    void should_prevent_saving_and_invalidate_account_book_when_storage_throws_constraint_violation() throws EventSequenceViolationException {
        // Given
        final var sponsorAccountId = AccountBook.AccountId.of(SponsorAccount.Id.random());
        final var accountBook = givenAccountBookInCache(sponsorAccountId);
        accountBook.mint(sponsorAccountId, PositiveAmount.of(8L));
        assertThat(accountBook.pendingEvents()).hasSize(1);

        // When
        when(accountBookEventStorage.getLastEventId(currency)).thenReturn(Optional.of(2L));
        doThrow(new DataIntegrityViolationException("duplicate key")).when(accountBookEventStorage).insert(any(), anyList());
        assertThatThrownBy(() -> cachedAccountBookProvider.save(currency, accountBook))
                .isInstanceOf(OnlyDustException.class)
                .hasCauseInstanceOf(EventSequenceViolationException.class)
                .hasMessageContaining("Could not save account book")
                .hasStackTraceContaining("Failed to insert events");

        // Then
        verify(accountBookEventStorage).insert(any(), anyList());
        assertThat(cachedAccountBookProvider.get(currency)).isNotSameAs(accountBook);
    }
}