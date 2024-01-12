package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public class AccountBookAggregate implements AccountBook {
    private final AccountBookState state = new AccountBookState();

    @Override
    public void mint(Account.Id account, PositiveAmount amount) {
        state.mint(account, amount);
    }

    @Override
    public void burn(Account.Id account, PositiveAmount amount) {
        state.burn(account, amount);
    }

    @Override
    public void transfer(Account.Id from, Account.Id to, PositiveAmount amount) {
        state.transfer(from, to, amount);
    }

    @Override
    public void refund(Account.Id from, Account.Id to, PositiveAmount amount) {
        state.refund(from, to, amount);
    }

    public AccountBookState state() {
        return state;
    }
}
