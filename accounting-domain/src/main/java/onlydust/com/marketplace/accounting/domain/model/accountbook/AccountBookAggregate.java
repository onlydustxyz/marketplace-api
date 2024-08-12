package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.EventType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AccountBookAggregate implements AccountBook {
    private final AccountBookState state = new AccountBookState();
    private final List<IdentifiedAccountBookEvent> pendingEvents = new ArrayList<>();
    private AccountBookObserver observer;
    private final Currency.Id currencyId;

    private long lastEventId = 0;

    public AccountBookAggregate observed(AccountBookObserver observer) {
        this.observer = observer;
        return this;
    }

    public static AccountBookAggregate empty(final @NonNull Currency.Id currencyId) {
        return new AccountBookAggregate(currencyId);
    }

    @Override
    public synchronized List<Transaction> mint(AccountId account, PositiveAmount amount) {
        return emit(new MintEvent(account, amount));
    }

    @Override
    public synchronized List<Transaction> burn(AccountId account, PositiveAmount amount) {
        return emit(new BurnEvent(account, amount));
    }

    @Override
    public synchronized List<Transaction> transfer(AccountId from, AccountId to, PositiveAmount amount) {
        return emit(new TransferEvent(from, to, amount));
    }

    @Override
    public synchronized List<Transaction> refund(AccountId from, AccountId to, PositiveAmount amount) {
        return emit(new RefundEvent(from, to, amount));
    }

    @Override
    public synchronized List<Transaction> refund(AccountId from) {
        return emit(new FullRefundEvent(from));
    }

    public ReadOnlyAccountBookState state() {
        return state;
    }

    public synchronized List<IdentifiedAccountBookEvent> pendingEvents() {
        return List.copyOf(pendingEvents);
    }

    public synchronized List<IdentifiedAccountBookEvent> getAndClearPendingEvents() {
        final var events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    public synchronized void receive(Collection<IdentifiedAccountBookEvent> events) {
        events.forEach(this::receive);
    }

    private <R> R receive(IdentifiedAccountBookEvent<R> event) {
        if (event.id() != nextEventId()) {
            throw new IllegalStateException("Invalid event id. Expected %d, got %d".formatted(nextEventId(), event.id()));
        }
        final var result = state.accept(event.data());
        incrementEventId();
        return result;
    }

    private List<Transaction> emit(AccountBookEvent<List<Transaction>> event) {
        final var result = state.accept(event);
        pendingEvents.add(new IdentifiedAccountBookEvent<>(nextEventId(), ZonedDateTime.now(), event));
        incrementEventId();
        if (observer != null)
            result.forEach(e -> observer.on(currencyId, e));

        return result;
    }

    private void incrementEventId() {
        ++lastEventId;
    }

    public synchronized long nextEventId() {
        return lastEventId + 1;
    }


    @EventType("Mint")
    public record MintEvent(@NonNull AccountId account, @NonNull PositiveAmount amount) implements AccountBookEvent<List<Transaction>> {
        @Override
        public List<Transaction> visit(AccountBookState state) {
            return state.mint(account, amount);
        }
    }

    @EventType("Burn")
    public record BurnEvent(@NonNull AccountId account, @NonNull PositiveAmount amount) implements AccountBookEvent<List<Transaction>> {
        @Override
        public List<Transaction> visit(AccountBookState state) {
            return state.burn(account, amount);
        }
    }

    @EventType("Transfer")
    public record TransferEvent(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) implements AccountBookEvent<List<Transaction>> {
        @Override
        public List<Transaction> visit(AccountBookState state) {
            return state.transfer(from, to, amount);
        }
    }

    @EventType("Refund")
    public record RefundEvent(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) implements AccountBookEvent<List<Transaction>> {
        @Override
        public List<Transaction> visit(AccountBookState state) {
            return state.refund(from, to, amount);
        }
    }

    @EventType("FullRefund")
    public record FullRefundEvent(@NonNull AccountId from) implements AccountBookEvent<List<Transaction>> {
        @Override
        public List<Transaction> visit(AccountBookState state) {
            return state.refund(from);
        }
    }
}
