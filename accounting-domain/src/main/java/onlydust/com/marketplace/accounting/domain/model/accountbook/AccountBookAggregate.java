package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
public class AccountBookAggregate implements AccountBook {
    private final AccountBookState state = new AccountBookState();
    private final List<IdentifiedAccountBookEvent> pendingEvents = new ArrayList<>();

    private long lastEventId = 0;

    public static AccountBookAggregate fromEvents(final @NonNull List<IdentifiedAccountBookEvent> events) {
        AccountBookAggregate aggregate = new AccountBookAggregate();
        aggregate.receive(events);
        return aggregate;
    }

    public static AccountBookAggregate empty() {
        return fromEvents(List.of());
    }

    @Override
    public void mint(AccountId account, PositiveAmount amount) {
        emit(new MintEvent(account, amount));
    }

    @Override
    public Collection<Transaction> burn(AccountId account, PositiveAmount amount) {
        return emit(new BurnEvent(account, amount));
    }

    @Override
    public void transfer(AccountId from, AccountId to, PositiveAmount amount) {
        emit(new TransferEvent(from, to, amount));
    }

    @Override
    public void refund(AccountId from, AccountId to, PositiveAmount amount) {
        emit(new RefundEvent(from, to, amount));
    }

    @Override
    public Set<AccountId> refund(AccountId from) {
        return emit(new FullRefundEvent(from));
    }

    public ReadOnlyAccountBookState state() {
        return state;
    }


    public List<IdentifiedAccountBookEvent> pendingEvents() {
        return List.copyOf(pendingEvents);
    }

    public void clearPendingEvents() {
        pendingEvents.clear();
    }

    public void receive(Collection<IdentifiedAccountBookEvent> events) {
        events.forEach(this::receive);
    }

    private <R> R receive(IdentifiedAccountBookEvent<R> event) {
        if (event.id() != nextEventId()) {
            throw new IllegalStateException("Invalid event id. Expected %d, got %d".formatted(nextEventId(), event.id()));
        }
        incrementEventId();
        return state.accept(event.data());
    }

    private <R> R emit(AccountBookEvent<R> event) {
        pendingEvents.add(new IdentifiedAccountBookEvent<>(nextEventId(), event));
        incrementEventId();
        return state.accept(event);
    }

    private void incrementEventId() {
        ++lastEventId;
    }

    public long nextEventId() {
        return lastEventId + 1;
    }


    @EventType("Mint")
    public record MintEvent(@NonNull AccountId account, @NonNull PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.mint(account, amount);
            return null;
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
    public record TransferEvent(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.transfer(from, to, amount);
            return null;
        }
    }

    @EventType("Refund")
    public record RefundEvent(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.refund(from, to, amount);
            return null;
        }
    }

    @EventType("FullRefund")
    public record FullRefundEvent(@NonNull AccountId from) implements AccountBookEvent<Set<AccountId>> {
        @Override
        public Set<AccountId> visit(AccountBookState state) {
            return state.refund(from);
        }
    }
}
