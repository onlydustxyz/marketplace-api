package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountBookAggregate implements AccountBook {
    private final AccountBookState state = new AccountBookState();
    private final List<AccountBookEvent> pendingEvents = new ArrayList<>();

    public static AccountBookAggregate fromEvents(final @NonNull Collection<AccountBookEvent> events) {
        AccountBookAggregate aggregate = new AccountBookAggregate();
        events.forEach(aggregate.state::accept);
        return aggregate;
    }

    public static AccountBookAggregate fromEvents(final @NonNull AccountBookEvent... events) {
        return fromEvents(List.of(events));
    }

    public static AccountBookAggregate empty() {
        return fromEvents();
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

    public AccountBookState state() {
        return state;
    }

    public List<AccountBookEvent> pendingEvents() {
        return pendingEvents;
    }

    private <R> R emit(AccountBookEvent<R> event) {
        pendingEvents.add(event);
        return state.accept(event);
    }

    public record MintEvent(@NonNull AccountId account, @NonNull PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.mint(account, amount);
            return null;
        }
    }

    public record BurnEvent(@NonNull AccountId account, @NonNull PositiveAmount amount) implements AccountBookEvent<List<Transaction>> {
        @Override
        public List<Transaction> visit(AccountBookState state) {
            return state.burn(account, amount);
        }
    }

    public record TransferEvent(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.transfer(from, to, amount);
            return null;
        }
    }

    public record RefundEvent(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.refund(from, to, amount);
            return null;
        }
    }

    public record FullRefundEvent(@NonNull AccountId from) implements AccountBookEvent<Set<AccountId>> {
        @Override
        public Set<AccountId> visit(AccountBookState state) {
            return state.refund(from);
        }
    }
}
