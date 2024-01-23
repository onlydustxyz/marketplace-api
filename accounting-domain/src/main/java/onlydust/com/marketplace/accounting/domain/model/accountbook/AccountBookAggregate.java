package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.AccountId;
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
    public void mint(AccountId account, PositiveAmount amount) {
        emit(new MintEvent(account, amount));
    }

    @Override
    public void burn(AccountId account, PositiveAmount amount) {
        emit(new BurnEvent(account, amount));
    }

    @Override
    public void transfer(AccountId from, AccountId to, PositiveAmount amount) {
        emit(new TransferEvent(from, to, amount));
    }

    @Override
    public void refund(AccountId from, AccountId to, PositiveAmount amount) {
        emit(new RefundEvent(from, to, amount));
    }

    public AccountBookState state() {
        return state;
    }

    public List<AccountBookEvent> pendingEvents() {
        return pendingEvents;
    }

    private void emit(AccountBookEvent event) {
        pendingEvents.add(event);
        state.accept(event);
    }

    public record MintEvent(AccountId account, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.mint(account, amount);
        }
    }

    public record BurnEvent(AccountId account, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.burn(account, amount);
        }
    }

    public record TransferEvent(AccountId from, AccountId to, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.transfer(from, to, amount);
        }
    }

    public record RefundEvent(AccountId from, AccountId to, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.refund(from, to, amount);
        }
    }
}
