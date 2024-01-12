package onlydust.com.marketplace.accounting.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;

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

    @Override
    public void mint(Account.Id account, PositiveAmount amount) {
        emit(new MintEvent(account, amount));
    }

    @Override
    public void burn(Account.Id account, PositiveAmount amount) {
        emit(new BurnEvent(account, amount));
    }

    @Override
    public void transfer(Account.Id from, Account.Id to, PositiveAmount amount) {
        emit(new TransferEvent(from, to, amount));
    }

    @Override
    public void refund(Account.Id from, Account.Id to, PositiveAmount amount) {
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

    public record MintEvent(Account.Id account, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.mint(account, amount);
        }
    }

    public record BurnEvent(Account.Id account, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.burn(account, amount);
        }
    }

    public record TransferEvent(Account.Id from, Account.Id to, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.transfer(from, to, amount);
        }
    }

    public record RefundEvent(Account.Id from, Account.Id to, PositiveAmount amount) implements AccountBookEvent {
        @Override
        public void visit(AccountBookState state) {
            state.refund(from, to, amount);
        }
    }
}
