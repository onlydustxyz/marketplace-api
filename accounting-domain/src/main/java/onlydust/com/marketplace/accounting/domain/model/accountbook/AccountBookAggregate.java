package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public void mint(Ledger.Id account, PositiveAmount amount) {
        emit(new MintEvent(account, amount));
    }

    @Override
    public Collection<Transaction> burn(Ledger.Id account, PositiveAmount amount) {
        return emit(new BurnEvent(account, amount));
    }

    @Override
    public void transfer(Ledger.Id from, Ledger.Id to, PositiveAmount amount) {
        emit(new TransferEvent(from, to, amount));
    }

    @Override
    public void refund(Ledger.Id from, Ledger.Id to, PositiveAmount amount) {
        emit(new RefundEvent(from, to, amount));
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

    public record MintEvent(Ledger.Id account, PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.mint(account, amount);
            return null;
        }
    }

    public record BurnEvent(Ledger.Id account, PositiveAmount amount) implements AccountBookEvent<List<Transaction>> {
        @Override
        public List<Transaction> visit(AccountBookState state) {
            return state.burn(account, amount);
        }
    }

    public record TransferEvent(Ledger.Id from, Ledger.Id to, PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.transfer(from, to, amount);
            return null;
        }
    }

    public record RefundEvent(Ledger.Id from, Ledger.Id to, PositiveAmount amount) implements AccountBookEvent<Void> {
        @Override
        public Void visit(AccountBookState state) {
            state.refund(from, to, amount);
            return null;
        }
    }
}
